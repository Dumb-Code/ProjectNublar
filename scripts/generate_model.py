while(True):
    name = input("name >> ")
    file = open("../src/main/resources/assets/projectnublar/models/block/" + name  + ".json", "w")
    file.write(
"""
{
  "loader": "dumblibrary:dcm",
  "parent": "block/cube_all",
  "textures": {
    "layer0": "projectnublar:block/fossil_processor"
  },
  "model": "projectnublar:models/block/""" + name + """"
}
""")
    file.close()