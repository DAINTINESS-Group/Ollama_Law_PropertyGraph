import json
import uuid
import pandas as pd
import numpy as np
from .prompts import extractConcepts
from .prompts import graphPrompt
from concurrent.futures import ThreadPoolExecutor


def documents2Dataframe(documents) -> pd.DataFrame:
    rows = []
    for chunk in documents:
        row = {
            "text": chunk.page_content,
            **chunk.metadata,

            #CHANGE HEREEEEEEEEEEEE -----------------------------------------------------------
            #"chunk_id": uuid.uuid4().hex,
            "chunk_id": chunk.metadata.get("paragraphID", "unknown")
        }
        rows = rows + [row]

    df = pd.DataFrame(rows)
    return df


def df2ConceptsList(dataframe: pd.DataFrame) -> list:
    # dataframe.reset_index(inplace=True)
    results = dataframe.apply(
        lambda row: extractConcepts(
            row.text, {"chunk_id": row.chunk_id, "type": "concept"}
        ),
        axis=1,
    )
    # invalid json results in NaN
    results = results.dropna()
    results = results.reset_index(drop=True)

    ## Flatten the list of lists to one single list of entities.
    concept_list = np.concatenate(results).ravel().tolist()
    return concept_list


def concepts2Df(concepts_list) -> pd.DataFrame:
    ## Remove all NaN entities
    concepts_dataframe = pd.DataFrame(concepts_list).replace(" ", np.nan)
    concepts_dataframe = concepts_dataframe.dropna(subset=["entity"])
    concepts_dataframe["entity"] = concepts_dataframe["entity"].apply(
        lambda x: x.lower()
    )

    return concepts_dataframe


def df2Graph(dataframe: pd.DataFrame, model=None) -> list:
    print(f" Processing {len(dataframe)} structured sections in df2Graph...")

    results = []
    for index, row in dataframe.iterrows():
        #print(f"⚡ Processing Row {index + 1}/{len(dataframe)}")
        
        # Convert row to JSON format for graphPrompt
        json_input = row.to_dict()  # Use full structured JSON


        #CHANGE HEREEEEEEEEEEEE -----------------------------------------------------------
        #response = graphPrompt(json.dumps(json_input), {"chunk_id": row.get("chunk_id", index)}, model)
        response = graphPrompt(json.dumps(json_input), {
            "chunk_id": row.get("chunk_id", index),
            "paragraph_id": row.get("paragraphID", "unknown")
        }, model)


        if response:
            results.append(response)
        else:
            if index % 100 == 0:  # Only print progress every 100 rows
                print(f" Processed {index + 1}/{len(dataframe)} rows...")

    if not results:
        print("Warning: No valid graph data extracted.")
        return []

    try:
        concept_list = np.concatenate(results).ravel().tolist()
    except ValueError:
        print("Error: Unable to concatenate graph results. Returning an empty list.")
        return []

    return concept_list

    



def graph2Df(nodes_list) -> pd.DataFrame:
    ## Remove all NaN entities

    #CHANGE HEREEEEEEEE -----------------------------------------------------------
    #graph_dataframe = pd.DataFrame(nodes_list).replace(" ", np.nan)
    graph_dataframe = pd.DataFrame(nodes_list).replace(" ", np.nan)
    graph_dataframe["paragraph_id"] = graph_dataframe["paragraph_id"].fillna("unknown")

    graph_dataframe = graph_dataframe.dropna(subset=["node_1", "node_2"])
    graph_dataframe["node_1"] = graph_dataframe["node_1"].apply(lambda x: x.lower())
    graph_dataframe["node_2"] = graph_dataframe["node_2"].apply(lambda x: x.lower())

    return graph_dataframe
