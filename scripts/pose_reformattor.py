import json
import os
from zipfile import ZipFile
from pprint import pprint


def addChild(jsonObject, childList):
    for json in jsonObject:
        childList.append(json)
        addChild(json["children"], childList)
    return

def getCubeInfo(filepath):
    zfile = ZipFile(filepath)
    for finfo in zfile.infolist():
        if finfo.filename == "model.json":
            file = zfile.open(finfo).read().decode("utf-8")
            data = json.loads(file)
            lis = []
            addChild(data["cubes"], lis)
            return lis

comareFiles = []
directory = input("Input File Directory")
if not directory.endswith("/"):
    directory += "/"
mainName = input("Main File Name")
main = getCubeInfo(directory + mainName)
inpDir = input("Pose File Location. Or leave blank to process the whole directory")
if inpDir != "":
    name = directory + inpDir
    comareFiles = [(name, getCubeInfo(name))]
else:
    for file in [f for f in os.listdir(directory) if os.path.isfile(os.path.join(directory, f))]:
        if file.endswith(".dcm") and file != mainName:
            name = directory + file
            comareFiles.append((name, getCubeInfo(name)))


for compareBit in comareFiles:
    compare = compareBit[1]
    finalOut = """{
  "version": 0,
  "overrides": ["""

    for cube in main:
        for otherCube in compare:
            if cube["name"] == otherCube["name"]:
                mainPos = cube["position"]
                otherPos = otherCube["position"]
                mainRot = cube["rotation"]
                otherRot = otherCube["rotation"]
                newline = "\n  "
                out = "\n{" + newline + "\"cube_name\":\"" + otherCube["name"] + "\""
                setOut = False
                if mainPos[0] != otherPos[0]:
                    out += "," + newline + "\"position_x\": " + str(otherPos[0])
                    setOut = True
                if mainPos[1] != otherPos[1]:
                    out += "," + newline + "\"position_y\": " + str(otherPos[1])
                    setOut = True
                if mainPos[2] != otherPos[2]:
                    out += "," + newline + "\"position_z\": " + str(otherPos[2])
                    setOut = True

                if mainRot[0] != otherRot[0]:
                    out += "," + newline + "\"rotation_x\": " + str(otherRot[0])
                    setOut = True
                if mainRot[1] != otherRot[1]:
                    out += "," + newline + "\"rotation_y\": " + str(otherRot[1])
                    setOut = True
                if mainRot[2] != otherRot[2]:
                    out += "," + newline + "\"rotation_z\": " + str(otherRot[2])
                    setOut = True

                out += "\n},"

                if setOut:
                    outLines = ""
                    for line in out.split("\n"):
                        if line == "":
                            pass;
                        else:
                            outLines += newline
                            outLines += "  " + line
                    finalOut += outLines

    finalOut
    finalOut += """  ]
}"""

    finalOut = finalOut.replace(",  ]", "\n  ]")

    file = open(compareBit[0][0:-4] + ".json", "w")
    file.write(finalOut)
    file.close()






