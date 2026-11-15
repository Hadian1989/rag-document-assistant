# RAG Eval Results

20 Testfragen (16 mit Antwort in den Dokumenten, 4 ohne).

| Chunk / Overlap | maxResults | Antwort korrekt | Quelle korrekt | Ablehnung korrekt |
|---|---|---|---|---|
| **1000 / 100** | **3** | **94 %** | 94 % | 100 % |
| 1000 / 100 | 5 | 94 % | 94 % | 100 % |
| 500 / 50 | 5 | 88 % | 100 % | 100 % |
| 300 / 30 | 5 | 81 % | 88 % | 100 % |

## Fazit

- **Beste Einstellung:** Chunk 1000 / Overlap 100, maxResults 3
- **Kleine Chunks** finden die richtige Seite genauer, aber Tabellen werden zerschnitten. Das LLM verwechselt dann Zeilen.
- **Große Chunks** behalten den Zusammenhang, darum sind die Antworten besser.
- **maxResults 3 statt 5:** gleiche Qualität, aber kürzerer Prompt = günstiger und schneller.
- **Ablehnung immer 100 %:** keine Halluzinationen bei Fragen außerhalb der Dokumente.