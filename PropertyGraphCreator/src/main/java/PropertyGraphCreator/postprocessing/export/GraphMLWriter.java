package PropertyGraphCreator.postprocessing.export;

import PropertyGraphCreator.model.graph.GraphEdge;
import PropertyGraphCreator.model.graph.GraphNode;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;

public class GraphMLWriter {

    public void writeGraph(String path, List<GraphNode> nodes, List<GraphEdge> edges) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element graphml = doc.createElement("graphml");
            graphml.setAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns");
            graphml.setAttribute("xmlns:y", "http://www.yworks.com/xml/graphml");
            doc.appendChild(graphml);

            // Define keys
            addKey(doc, graphml, "d0", "node", "label");
            addKey(doc, graphml, "d1", "edge", "label");
            addKey(doc, graphml, "d2", "edge", "paragraph_id");

            Element graph = doc.createElement("graph");
            graph.setAttribute("edgedefault", "directed");
            graphml.appendChild(graph);

            for (GraphNode node : nodes) {
                Element nodeElem = doc.createElement("node");
                nodeElem.setAttribute("id", node.getId());
                graph.appendChild(nodeElem);

                Element data = doc.createElement("data");
                data.setAttribute("key", "d0");
                nodeElem.appendChild(data);

                Element shapeNode = doc.createElement("y:ShapeNode");
                Element nodeLabel = doc.createElement("y:NodeLabel");
                nodeLabel.setTextContent(node.getLabel());
                shapeNode.appendChild(nodeLabel);
                data.appendChild(shapeNode);
            }

            for (GraphEdge edge : edges) {
                Element edgeElem = doc.createElement("edge");
                edgeElem.setAttribute("source", edge.getSource());
                edgeElem.setAttribute("target", edge.getTarget());
                graph.appendChild(edgeElem);

                // Edge label data
                Element labelData = doc.createElement("data");
                labelData.setAttribute("key", "d1");
                edgeElem.appendChild(labelData);

                Element polyLineEdge = doc.createElement("y:PolyLineEdge");
                Element edgeLabel = doc.createElement("y:EdgeLabel");
                edgeLabel.setTextContent(edge.getLabel());
                polyLineEdge.appendChild(edgeLabel);
                labelData.appendChild(polyLineEdge);

                // Paragraph ID data
                Element pidData = doc.createElement("data");
                pidData.setAttribute("key", "d2");
                pidData.setTextContent(edge.getParagraphId());
                edgeElem.appendChild(pidData);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));
            transformer.transform(source, result);

            System.out.println("✅ GraphML saved to: " + path);

        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private void addKey(Document doc, Element root, String id, String type, String name) {
        Element key = doc.createElement("key");
        key.setAttribute("id", id);
        key.setAttribute("for", type);
        key.setAttribute("attr.name", name);
        key.setAttribute("attr.type", "string");
        root.appendChild(key);
    }
}
