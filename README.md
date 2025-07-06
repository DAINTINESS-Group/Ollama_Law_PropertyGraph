# Ollama_Law_PropertyGraph: Automated Analysis of Regulatory Documents
---

## 🔍 Overview

This project provides a **fully modular system** for the **automated extraction, representation, and exploration of legal knowledge** from EU legislative texts such as the:

- **AI Act**
- **GDPR**
- **Data Act**
- **Data Governance Act**

Unlike cloud-based solutions, this pipeline runs entirely **locally** using **Ollama** and **Zephyr 7B**, ensuring **privacy** and full control over legal data.

It consists of three distinct tools:

- **Legislative Text Parser** – Parses a legal PDF into structured JSON and extracts key legal entities.
- **Property Graph Creator** – Uses Zephyr 7B to extract relationships between entities and outputs a filtered, normalized GraphML.
- **Entity Checklist Generator** – Visualizes the graph and provides Markdown reports for compliance and audit purposes.

---

## 🧱 Pipeline Architecture

![Screenshot_1](https://github.com/user-attachments/assets/8a8e98ef-7b26-4d39-8973-bfd71be7a2e6)

---

## 🧩 Modules

### 1. **Legislative Text Parser** (Java & Swing)

- Converts PDF legal documents into structured JSON.
- Breaks text into **Chapters**, **Articles**, **Paragraphs**, and **Points**.
- Extracts definitions and legal entities.
- Visual UI allows navigation of legal text structure.
- **Output**: JSON (structure + entities)

---

### 2. **Property Graph Creator** (JavaFX + Python + Ollama LLM)

- Uses Zephyr 7B via **Ollama** to extract legal relationships.
- Builds a **GraphML** property graph using **NetworkX**.
- Executes custom post-processing:
  - Node normalization
  - Entity merging
  - Edge filtering
- **Output**: `graph.graphml` (raw), `subgraph.graphml` (cleaned)

---

### 3. **Entity Checklist Generator** (JavaFX)

- Loads final GraphML and JSON to:
  - Display all entities and their relations
  - Link them to their original legal context
- Exports a **Markdown report checklist**
- Enables **interactive exploration** for legal analysts

---

## 🧪 Technologies Used

| Category                  | Tools / Frameworks                                           |
| ------------------------- | ------------------------------------------------------------ |
| **Programming Languages** | Java (19+), Python (3.10+)                                   |
| **UI Frameworks**         | Java Swing (Legislative Text Parser), JavaFX (Checklist UI)  |
| **LLM Execution**         | [Ollama](https://ollama.com) + [Zephyr-7B](https://huggingface.co/HuggingFaceH4/zephyr-7b-alpha) |
| **NLP Libraries**         | NLTK, Stanford CoreNLP                                       |
| **Graph Libraries**       | NetworkX (Python), Custom Java Graph Model + GraphML         |
| **Visualization**         | yEd-compatible GraphML output                                |
| **Parsing & I/O**         | lxml, JSON, Markdown generation                              |
| **Build Tools**           | Maven (Java), pip (Python)                                   |
| **Benchmarking**          | Precision/Recall analysis, MT-Bench (model eval)             |

---

## 🛠️ Installation

### System Requirements

- **OS**: Windows 10+ / Linux / macOS
- **RAM**: 16 GB recommended
- **Java**: Version 19+
- **Python**: Version 3.10+
- **CPU**: AVX2 support required for Ollama

---

### Setup Steps

#### 1. Install Ollama & Zephyr

```bash
# Download and install from:
https://ollama.com

# Run the Zephyr model locally
ollama run zephyr
```

#### 2. Build Java Components

```bash
mvn clean package
```

#### 3. Set Up Python Environment

```bash
pip install nltk networkx matplotlib graphviz lxml
```

---

## 🚧 Future Work

- Support for newer LLMs (e.g., GPT-4 via API)
- Parallel LLM chunk processing
- Unified GUI for all tools
- Improved model prompting & benchmarking

---

## 🔗 References

- [Zephyr-7B (Hugging Face)](https://huggingface.co/HuggingFaceH4/zephyr-7b-alpha)
- [Ollama](https://ollama.com)
- [EU AI Act Summary](https://iapp.org/media/pdf/resource_center/eu-ai-act-101-chart.pdf)
- [GDPR Summary](https://iapp.org/media/pdf/resource_center/gdpr-101-chart.pdf)

---

## 👨‍💼 Authors

**Bouzampalidis Vasileios Ioannis**  
Department of Computer Science & Engineering, University of Ioannina  
**Supervisor**: Prof. Panagiotis Vassiliadis
