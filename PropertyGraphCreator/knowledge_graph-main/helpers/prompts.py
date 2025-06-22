import logging
import sys
sys.path.append("..")

import json
import ollama.client as client


def extractConcepts(prompt: str, metadata={}, model="mistral-openorca:latest"):
    #SYS_PROMPT = (
    #    "Your task is extract the key concepts (and non personal entities) mentioned in the given context. "
     #   "Extract only the most important and atomistic concepts, if  needed break the concepts down to the simpler concepts."
      #  "Categorize the concepts in one of the following categories: "
       # "[event, concept, place, object, document, organization, condition, misc]\n"
        #"Format your output as a list of json with the following format:\n"
        #"[\n"
        #"   {\n"
        #'       "entity": The Concept,\n'
        #'       "importance": The concontextual importance of the concept on a scale of 1 to 5 (5 being the highest),\n'
        #'       "category": The Type of Concept,\n'
        #"   }, \n"
        #"{ }, \n"
        #"]\n"
    #)
    SYS_PROMPT = (
        "You are processing a structured legal document in JSON format. "
        "Extract only the most relevant legal concepts. "
        "DO NOT extract common words—focus only on legal definitions, obligations, rights, and restrictions.\n\n"

        "### Expected JSON Output:\n"
        "[\n"
        "   {\n"
        '       "entity": "The legal concept",\n'
        '       "importance": "Contextual importance on a scale of 1 to 5",\n'
        '       "category": "One of [law, regulation, obligation, restriction, right, entity, organization, procedure, condition, exception]"\n'
        "   }\n"
        "]\n\n"

        "### Strict Rules:\n"
        "- **DO NOT RETURN EXPLANATIONS.**\n"
        "- **DO NOT INCLUDE TEXT DESCRIBING HOW THE JSON IS GENERATED.**\n"
        "- **RETURN ONLY A VALID JSON ARRAY.**\n"
        "- **THE FIRST CHARACTER MUST BE '[' AND THE LAST CHARACTER MUST BE ']'**\n"
    )

    response, _ = client.generate(model_name=model, system=SYS_PROMPT, prompt=prompt)

    response = response.strip()  # Remove extra spaces

    if not response.startswith("[") or not response.endswith("]"):
        print(f" ERROR: Model returned unexpected text instead of JSON:\n{response[:200]}")
        return []  # Prevent pipeline failure

    try:
        result = json.loads(response)  # Ensure valid JSON
        result = [dict(item, **metadata) for item in result]  # Add metadata
        return result
    except json.JSONDecodeError:
        print(f" ERROR: Model returned invalid JSON!\n{response}")
        return []
    

# Load entities.txt dynamically
def load_entities():
    try:
        with open("G:/DIPLOMATIKI/KG_code/knowledge_graph-main/data_input/cureus/entities.txt", "r", encoding="utf-8") as f:
            entities = [line.split(" means")[0].strip() for line in f.readlines() if " means" in line]
        return entities
    except Exception as e:
        logging.warning(f"Error loading entities.txt: {e}")
        return []


def graphPrompt(input_json: str, metadata={}, model="mistral-openorca:latest"):

    # model_info = client.show(model_name=model)
    # print( chalk.blue(model_info))

    #SYS_PROMPT = (
        #"You are a network graph maker who extracts terms and their relations from a given context. "
       # "As a graph maker, you are provided with a context chunk (delimited by ```) Your task is to extract the ontology "
       # "of terms mentioned in the given context. These terms should represent the key concepts as per the context. \n"
        #"Thought 1: While traversing through each sentence, Think about the key terms mentioned in it.\n"
         #   "\tTerms may include object, entity, location, organization, person, \n"
          #  "\tcondition, acronym, documents, service, concept, etc.\n"
           # "\tTerms should be as atomistic as possible\n\n"
        #"Thought 2: Think about how these terms can have one on one relation with other terms.\n"
         #   "\tTerms that are mentioned in the same sentence or the same paragraph are typically related to each other.\n"
          #  "\tTerms can be related to many other terms\n\n"
        #"Thought 3: Find out the relation between each such related pair of terms. \n\n"
        #"Format your output as a list of json. Each element of the list contains a pair of terms"
        #"and the relation between them, like the follwing: \n"
        #"[\n"
        #"   {\n"
        #'       "node_1": "A concept from extracted ontology",\n'
        #'       "node_2": "A related concept from extracted ontology",\n'
        #'       "edge": "relationship between the two concepts, node_1 and node_2 in one or two sentences"\n'
        #"   }, {...}\n"
        #"]"
    #)

    entities_list = load_entities()
    valid_entities = set(entities_list)  # Remove duplicates and create a fast lookup set

    # Convert to a string for the prompt
    entities_text = ", ".join(valid_entities)

    SYS_PROMPT = (
            "You are an AI system designed to extract relationships between strictly predefined legal entities.\n"
            "And I am human collaborator, providing instructions, input data and verifying that your output matches the expected legal structure and terminology.\n"
            "You will now find the full list of valid legal entities below. Use this list as your sole reference when deciding what to extract:\n"
            f"{entities_text}\n\n"

            "### STEP 1: **Extract Terms and Relationships**\n"
            "1. Nodes (terms):\n"
            "   - Node labels (node_1 AND node_2) **MUST** be short (2-5 words) — CASE SENSITIVE.\n"
            "   - Target entity **MUST** match the predefined entity list — CASE SENSITIVE.\n"
            "   - NO inferred or modified terms allowed \n\n"

            "2. Relationships (edges):\n"
            "   - Edge labels **MUST** be **short** (2-5 words) — CASE SENSITIVE.\n"
            "   - Only extract direct and logically inferred connections \n"
            "   - Do not extract vague terms like 'related to' or 'connected with' \n\n"

            "3. STRICT RULES:\n"
            "   - If the target entity is not from the predefined entity list → SKIP\n"
            "   - If no valid relationship exists → Return an **empty array** `[]`\n\n"
            " **Normalize Terms**\n"
            "- If two nodes are contextually or semantically similar, even if they differ slightly in wording (e.g., providers of AI systems vs. providers), normalize them into a single node.\n"

            "### STEP 2: **Output Format**\n"
            "- Include paragraph_id from the input metadata in the output. This ID refers to the specific paragraph from which the relationship was extracted. Maintain the original format (e.g., 1.1.1).\n"
            "- Each relationship must include paragraph_id to indicate the exact paragraph from which the relationship was extracted.\n"

            "### STEP 3: **Encourage Cross-Connections**\n"
            "- Extract logical relationships across different paragraphs and sections.\n"
            "- If an entity refers to another entity mentioned in a previous paragraph, establish a relationship between them.\n"


            "Return the extracted relationships in this JSON format:\n"
            "[\n"
            "   {\n"
            '       "node_1": "Entity from the predefined list (2-5 words)",\n'
            '       "node_2": "Another entity related to the entity from node_1 (2-5 words)",\n'
            '       "edge": "Concise relationship label (2-5 words)",\n'
            '       "paragraph_id": "1.1.2"\n'
            "   },\n"
            "   {...}\n"
            "]\n\n"

    )

    USER_PROMPT = ( f"Legal Document Context:\n\n{input_json} Extract relationships and return a JSON array:" 
        
    )




    
    response, _ = client.generate(model_name=model, system=SYS_PROMPT, prompt=USER_PROMPT)

    try:
        result = json.loads(response)
        result = [dict(item, **metadata) for item in result]
    except Exception as e:
        logging.warning(f"Invalid response at row {metadata.get('chunk_id', 'Unknown')} - Exception: {e}")
        result = None
    return result


