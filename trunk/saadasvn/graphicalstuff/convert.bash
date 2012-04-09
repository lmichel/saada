#! /bin/bash

IMAGES="CommentClass CommentCollection CommentRel ConeSearch CreateColl CreateIndex
CreateRel DropClass DropCollection DropIndex DropRel DropRelIndex EmptyCategory
EmptyClass EmptyCollection EmptyRel Filter IndexRel IndexTable ivoa LoadData
ManageData MetaData ObsCore PopulateRel Relation RemoveData Satistics SelectFilter
SIA SSA TAP Tool UserModel VOPublish VOServices Web"

for IMG in $IMAGES
do
convert -resize 96 $IMG.bmp ../resources/icons/$IMG.png
done