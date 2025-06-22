package PropertyGraphCreator.postprocessing.export;

import PropertyGraphCreator.model.graph.GraphEdge;
import PropertyGraphCreator.model.graph.GraphNode;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GraphMLLoader {

    public List<GraphNode> loadNodes(String pathToGraphML) {
        List<GraphNode> nodes = new ArrayList<>();
        try {
            Document doc = parseXML(pathToGraphML);
            NodeList nodeElements = doc.getElementsByTagName("node");

            for (int i = 0; i < nodeElements.getLength(); i++) {
                Element nodeElem = (Element) nodeElements.item(i);
                String id = nodeElem.getAttribute("id");
                String label = extractLabel(nodeElem);
                nodes.add(new GraphNode(id, label));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return nodes;
    }

    public List<GraphEdge> loadEdges(String pathToGraphML) {
        List<GraphEdge> edges = new ArrayList<>();
        try {
            Document doc = parseXML(pathToGraphML);
            NodeList edgeElements = doc.getElementsByTagName("edge");

            for (int i = 0; i < edgeElements.getLength(); i++) {
                Element edgeElem = (Element) edgeElements.item(i);
                String source = edgeElem.getAttribute("source");
                String target = edgeElem.getAttribute("target");
                String label = extractLabel(edgeElem);
                String paragraphId = extractData(edgeElem, "d2");
                edges.add(new GraphEdge(source, target, label, paragraphId));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return edges;
    }

    private Document parseXML(String path) throws Exception {
        File file = new File(path);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }

    private String extractLabel(Element element) {
        NodeList dataList = element.getElementsByTagName("y:NodeLabel");
        if (dataList.getLength() == 0) {
            dataList = element.getElementsByTagName("y:EdgeLabel");
        }
        if (dataList.getLength() > 0) {
            return dataList.item(0).getTextContent();
        }
        return "";
    }

    private String extractData(Element element, String key) {
        NodeList dataList = element.getElementsByTagName("data");
        for (int i = 0; i < dataList.getLength(); i++) {
            Element dataElem = (Element) dataList.item(i);
            if (key.equals(dataElem.getAttribute("key"))) {
                return dataElem.getTextContent();
            }
        }
        return "";
    }
}

