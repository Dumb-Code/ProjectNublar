while(True):
    for name in input("names >>").split(" "):
        file = open("../src/main/resources/assets/projectnublar/models/item/" + name  + ".json", "w")
        file.write(
"""{
  "parent": "projectnublar:block/""" + name + """"
}""")
        file.close()