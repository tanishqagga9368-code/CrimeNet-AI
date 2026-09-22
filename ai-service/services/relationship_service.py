def extract_relationships(entities):

    relationships = []

    for i in range(len(entities)):
        for j in range(i + 1, len(entities)):

            first = entities[i]
            second = entities[j]

            if first["label"] == "PERSON" and second["label"] == "ORG":
                relationships.append({
                    "source": first["text"],
                    "relation": "ASSOCIATED_WITH",
                    "target": second["text"]
                })

            elif first["label"] == "PERSON" and second["label"] == "PERSON":
                relationships.append({
                    "source": first["text"],
                    "relation": "CONNECTED_TO",
                    "target": second["text"]
                })

    return relationships