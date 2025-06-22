package EntityChecklistGenerator.parser;

import EntityChecklistGenerator.model.graph.GraphContainer;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses a GraphML file (of the form your generator emits)
 * into our in‑memory Graph model.
 */
public class GraphMlParser implements Parser<GraphContainer> {

    /**
     * @param file a ready GraphML (.graphml) file
     * @return a populated Graph (with all nodes and edges, including paragraphId)
     */

    @Override
    public GraphContainer parse(File file) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);

        Map<String,KeyInfo> keyMap = new HashMap<>();
        NodeList keyList = doc.getElementsByTagNameNS("*", "key");
        for (int i = 0; i < keyList.getLength(); i++) {
            Element keyEl = (Element) keyList.item(i);
            String id       = keyEl.getAttribute("id");
            String forType  = keyEl.getAttribute("for");
            String attrName = keyEl.getAttribute("attr.name");
            keyMap.put(id, new KeyInfo(forType, attrName));
        }

        GraphContainer graph = new GraphContainer();

        NodeList nodes = doc.getElementsByTagNameNS("*", "node");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element nodeEl = (Element) nodes.item(i);
            String id = nodeEl.getAttribute("id");
            String label = id;  // fallback

            NodeList dataElems = nodeEl.getElementsByTagNameNS("*", "data");
            for (int j = 0; j < dataElems.getLength(); j++) {
                Element dataEl = (Element) dataElems.item(j);
                KeyInfo ki = keyMap.get(dataEl.getAttribute("key"));
                if ("node".equals(ki.forType) && "label".equals(ki.attrName)) {
                    label = dataEl.getTextContent().trim();
                }
            }

            graph.addNode(id, label);
        }

        NodeList edges = doc.getElementsByTagNameNS("*", "edge");
        for (int i = 0; i < edges.getLength(); i++) {
            Element edgeEl = (Element) edges.item(i);
            String src = edgeEl.getAttribute("source");
            String tgt = edgeEl.getAttribute("target");
            String label = "";
            String paragraphId = null;

            NodeList dataElems = edgeEl.getElementsByTagNameNS("*", "data");
            for (int j = 0; j < dataElems.getLength(); j++) {
                Element dataEl = (Element) dataElems.item(j);
                KeyInfo ki = keyMap.get(dataEl.getAttribute("key"));
                if ("edge".equals(ki.forType)) {
                    if ("label".equals(ki.attrName)) {
                        label = dataEl.getTextContent().trim();
                    } else if ("paragraph_id".equals(ki.attrName)) {
                        paragraphId = dataEl.getTextContent().trim();
                    }
                }
            }

            graph.addEdge(src, tgt, label, paragraphId);
        }

        return graph;
    }

    /** Simple holder for the <key> metadata. */
    private static class KeyInfo {
        final String forType;
        final String attrName;
        KeyInfo(String forType, String attrName) {
            this.forType = forType;
            this.attrName = attrName;
        }
    }
}
