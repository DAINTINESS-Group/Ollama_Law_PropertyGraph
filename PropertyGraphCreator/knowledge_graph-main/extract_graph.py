import pandas as pd
import numpy as np
import os
#from langchain.document_loaders import PyPDFLoader, UnstructuredPDFLoader, PyPDFium2Loader
#from langchain.document_loaders import PyPDFDirectoryLoader, DirectoryLoader
#from langchain.text_splitter import RecursiveCharacterTextSplitter
from pathlib import Path
import random
import time

start_time = time.time()  # Start timing

data_dir = "cureus"
inputdirectory = Path(f"./data_input/{data_dir}")
out_dir = data_dir
outputdirectory = Path(f"./data_output/{out_dir}")

import json
from langchain_text_splitters import RecursiveCharacterTextSplitter
import pandas as pd

json_file_path = inputdirectory / "test.json"

with open(json_file_path, "r", encoding="utf-8") as f:
    data = json.load(f)



def flatten_json(json_data):
    flat_data = []
    
    # Iterate directly over the list of chapters (since there's no 'children' key)
    for chapter in json_data:
        for article in chapter.get("articles", []):
            for paragraph in article.get("paragraphs", []):
                # Extract paragraph ID and combined text content
                paragraph_id = paragraph.get("paragraphID", "unknown")
                text = " ".join(
                    [t["text"].strip() for t in paragraph.get("text", []) if isinstance(t.get("text"), str)]
                )
                if text:  # Only include non-empty paragraphs
                    flat_data.append({
                        "paragraphID": paragraph_id,
                        "text": text
                    })
    
    return flat_data



#def extract_text_from_json(json_data):
    text_chunks = []
    
    for chapter in json_data.get("children", []):
        for article in chapter.get("articles", []):
            for paragraph in article.get("paragraphs", []):
                text_content = " ".join(
                    [t["text"].strip() for t in paragraph.get("text", []) if isinstance(t.get("text"), str)]
                )
                text_chunks.append(text_content)

    return text_chunks  

#documents = extract_text_from_json(data)
documents = flatten_json(data)


if not documents:
    raise ValueError("No valid text found in the JSON file.")

# Define text splitter
splitter = RecursiveCharacterTextSplitter(
    chunk_size=1500,
    chunk_overlap=150,
    length_function=len,
    is_separator_regex=False,
)




from langchain.schema import Document
from helpers.df_helpers import documents2Dataframe

#CHANGE HEREEEEEEEEEEEEEE --------------------------------------------------------------------------------------------
#documents = [Document(page_content=chunk, metadata={"source": "json"}) for chunk in pages]
documents = [
    Document(
        page_content=chunk["text"],
        metadata={
            "source": "json",
            "paragraphID": chunk.get("paragraphID", "unknown")
        }
    )
    for chunk in documents
]


df = documents2Dataframe(documents)

#df.head()


## This function uses the helpers/prompt function to extract concepts from text
from helpers.df_helpers import df2Graph
from helpers.df_helpers import graph2Df

## To regenerate the graph with LLM, set this to True
regenerate = True

if regenerate:
    required_columns = ["chapter", "article", "paragraph"]
    for col in required_columns:
        if col not in df.columns:
            df[col] = "Unknown"  

    concepts_list = df2Graph(df, model='zephyr:latest')
    dfg1 = graph2Df(concepts_list)
    
    if not os.path.exists(outputdirectory):
        os.makedirs(outputdirectory)

    dfg1.to_csv(outputdirectory / "graph.csv", sep="|", index=False)
    df.to_csv(outputdirectory / "chunks.csv", sep="|", index=False)
else:
    dfg1 = pd.read_csv(outputdirectory / "graph.csv", sep="|")

dfg1.replace("", np.nan, inplace=True)
dfg1.dropna(subset=["node_1", "node_2", 'edge'], inplace=True)
dfg1['count'] = 4  


#print(dfg1.shape)
#dfg1.head()



def contextual_proximity(df: pd.DataFrame) -> pd.DataFrame:
    ## Melt the dataframe into a list of nodes
    dfg_long = pd.melt(
        df, id_vars=["chunk_id"], value_vars=["node_1", "node_2"], value_name="node"
    )
    dfg_long.drop(columns=["variable"], inplace=True)
    # Self join with chunk id as the key will create a link between terms occuring in the same text chunk.
    dfg_wide = pd.merge(dfg_long, dfg_long, on="chunk_id", suffixes=("_1", "_2"))
    # drop self loops
    self_loops_drop = dfg_wide[dfg_wide["node_1"] == dfg_wide["node_2"]].index
    dfg2 = dfg_wide.drop(index=self_loops_drop).reset_index(drop=True)
    ## Group and count edges.
    dfg2 = (
        dfg2.groupby(["node_1", "node_2"])
        .agg({"chunk_id": [",".join, "count"]})
        .reset_index()
    )
    dfg2.columns = ["node_1", "node_2", "chunk_id", "count"]
    dfg2.replace("", np.nan, inplace=True)
    dfg2.dropna(subset=["node_1", "node_2"], inplace=True)
    # Drop edges with 1 count
    dfg2 = dfg2[dfg2["count"] != 1]
    dfg2["edge"] = "contextual proximity"
    return dfg2


dfg2 = contextual_proximity(dfg1)
dfg2.tail()

dfg2 = dfg2[dfg2["edge"] != "contextual proximity"]

dfg = pd.concat([dfg1, dfg2], axis=0)
dfg = (
    dfg.groupby(["node_1", "node_2"])
    .agg({
        "chunk_id": ",".join, 
        "edge": ','.join, 
        "count": 'sum',
        "paragraph_id": ','.join
    })
    .reset_index()
)

dfg


nodes = pd.concat([dfg['node_1'], dfg['node_2']], axis=0).unique()
nodes.shape

import networkx as nx
G = nx.Graph()

## Add nodes to the graph
for node in nodes:
    G.add_node(str(node), label=str(node))

## Add edges to the graph
for index, row in dfg.iterrows():
    if row["edge"] != "contextual proximity": 
        G.add_edge(
            str(row["node_1"]),
            str(row["node_2"]),
            label=row["edge"],
            weight=row['count']/4,
            paragraph_id=row.get('paragraph_id', 'unknown')  # <-- Include paragraph_id here
        )

from lxml import etree
import os

def write_graphml_correctly(G, path):
    # Define namespaces for yEd compatibility
    nsmap = {
        None: "http://graphml.graphdrawing.org/xmlns",
        'y': "http://www.yworks.com/xml/graphml"
    }

    # Create root element
    root = etree.Element("graphml", nsmap=nsmap)

    key_node = etree.SubElement(root, "key", attrib={
        "id": "d0",
        "for": "node", 
        "attr.name": "label",
        "attr.type": "string"
    })

    key_edge = etree.SubElement(root, "key", attrib={
        "id": "d1",
        "for": "edge",  
        "attr.name": "label",
        "attr.type": "string"
    })


    key_paragraph = etree.SubElement(root, "key", attrib={
        "id": "d2",
        "for": "edge",  
        "attr.name": "paragraph_id",
        "attr.type": "string"
    })



    # Create graph element
    graph = etree.SubElement(root, "graph", edgedefault="directed")

    # Add nodes with yEd compatible formatting
    for node in G.nodes(data=True):
        node_id = node[0]
        label = node[1].get('label', node_id)

        # Node definition
        node_element = etree.SubElement(graph, "node", id=node_id)
        data = etree.SubElement(node_element, "data", key="d0")

        shape_node = etree.SubElement(data, "{http://www.yworks.com/xml/graphml}ShapeNode")
        node_label = etree.SubElement(shape_node, "{http://www.yworks.com/xml/graphml}NodeLabel")
        node_label.text = label

    # Add edges with yEd compatible formatting
    for edge in G.edges(data=True):
        source = edge[0]
        target = edge[1]
        label = edge[2].get('label', '')
        weight = edge[2].get('weight', 1.0)

        # Edge definition
        edge_element = etree.SubElement(graph, "edge", source=source, target=target)
        data = etree.SubElement(edge_element, "data", key="d1")

        paragraph_data = etree.SubElement(edge_element, "data", key="d2")
        paragraph_data.text = edge[2].get('paragraph_id', 'unknown')


        polyline_edge = etree.SubElement(data, "{http://www.yworks.com/xml/graphml}PolyLineEdge")
        edge_label = etree.SubElement(polyline_edge, "{http://www.yworks.com/xml/graphml}EdgeLabel")
        edge_label.text = label

    # Write to file
    tree = etree.ElementTree(root)
    tree.write(path, pretty_print=True, xml_declaration=True, encoding="utf-8")

    print(f"Correct GraphML file saved at: {path}")

# Example usage:
graphml_output_path = "./docs/graph.graphml"
write_graphml_correctly(G, graphml_output_path)


communities_generator = nx.community.girvan_newman(G)
top_level_communities = next(communities_generator)
next_level_communities = next(communities_generator)
communities = sorted(map(sorted, next_level_communities))

import seaborn as sns
palette = "hls"

## Now add these colors to communities and make another dataframe
def colors2Community(communities) -> pd.DataFrame:
    ## Define a color palette
    p = sns.color_palette(palette, len(communities)).as_hex()
    random.shuffle(p)
    rows = []
    group = 0
    for community in communities:
        color = p.pop()
        group += 1
        for node in community:
            rows += [{"node": node, "color": color, "group": group}]
    df_colors = pd.DataFrame(rows)
    return df_colors


colors = colors2Community(communities)
colors

for index, row in colors.iterrows():
    G.nodes[row['node']]['group'] = row['group']
    G.nodes[row['node']]['color'] = row['color']
    G.nodes[row['node']]['size'] = G.degree[row['node']]

from pyvis.network import Network

graph_output_directory = "./docs/index.html"

net = Network(
    notebook=False,
    # bgcolor="#1a1a1a",
    cdn_resources="remote",
    height="900px",
    width="100%",
    select_menu=True,
    # font_color="#cccccc",
    filter_menu=False,
)

net.from_nx(G)

# net.repulsion(node_distance=150, spring_length=400)

net.force_atlas_2based(central_gravity=0.015, gravity=-31)
# net.barnes_hut(gravity=-18100, central_gravity=5.05, spring_length=380)
net.show_buttons(filter_=["physics"])

net.show(graph_output_directory, notebook=False)

# Inject JavaScript into index.html AFTER generating the file
with open(graph_output_directory, "a") as f:  # "a" means append to the existing file
    f.write("""
    <script type="text/javascript">
        function deleteNode() {
            var selectedNodes = network.getSelectedNodes();
            if (selectedNodes.length === 0) {
                alert("Please select a node to delete.");
                return;
            }

            // Remove selected nodes and their edges
            network.body.data.nodes.remove(selectedNodes);
            network.body.data.edges.remove(network.getConnectedEdges(selectedNodes[0]));

            alert("Node and connected edges deleted!");
        }

        // Add a delete button dynamically
        var deleteButton = document.createElement("button");
        deleteButton.innerHTML = "Delete Selected Node";
        deleteButton.onclick = deleteNode;
        deleteButton.style.position = "absolute";
        deleteButton.style.top = "10px";
        deleteButton.style.right = "10px";
        deleteButton.style.padding = "10px";
        deleteButton.style.backgroundColor = "#ff4d4d";
        deleteButton.style.color = "white";
        deleteButton.style.border = "none";
        deleteButton.style.cursor = "pointer";
        document.body.appendChild(deleteButton);

        function saveGraph() {
            var nodes = network.body.data.nodes.get(); // Get remaining nodes
            var edges = network.body.data.edges.get(); // Get remaining edges

            var graphData = {
                edges: edges.map(edge => ({
                    "node_1": edge.from,
                    "node_2": edge.to,
                    "edge": edge.label || "relationship"
                }))
            };

            // Convert to JSON and trigger file download
            var blob = new Blob([JSON.stringify(graphData, null, 2)], { type: "application/json" });
            var a = document.createElement("a");
            a.href = URL.createObjectURL(blob);
            a.download = "final_graph.json";
            a.click();
        }

        // Add a "Save Graph" button dynamically
        var saveButton = document.createElement("button");
        saveButton.innerHTML = "Save Graph";
        saveButton.onclick = saveGraph;
        saveButton.style.position = "absolute";
        saveButton.style.top = "50px";  // Adjusted to avoid overlapping
        saveButton.style.right = "10px";
        saveButton.style.padding = "10px";
        saveButton.style.backgroundColor = "#4CAF50";
        saveButton.style.color = "white";
        saveButton.style.border = "none";
        saveButton.style.cursor = "pointer";
        document.body.appendChild(saveButton);
    </script>
    """)


# End of script
end_time = time.time()
print(f"Total Execution Time: {end_time - start_time:.2f} seconds")



