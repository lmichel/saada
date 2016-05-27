#!/bin/bash
#
# Minifier 
# Laurent Michel 02/2014
#
# Merge all required JS file in one m
# Merge all required in one. 
# Using the minifierd resources speed the application startup and make it indepen,dant from jsresources
# Warning: All resources used by the CSS (image with relative paths or imoprted CSS) must be available in the min directory
# A Part of the resources are local to 3XMM and the others are copied from jsresources
#

#-----------------------------------------------------------
# Script Resources
#-----------------------------------------------------------   
outputDir="../web/min/packed"      # directory where both packed JS and CSS are stored 
packedCSS=$outputDir/packedCSS.css # name of the file containing the packed CSS
packedJS=$outputDir/packedJS.js    # name of the file containing the packed JS
imageDir="../web/images"           # Directory from where the 3XMM images must be copied 
imageOutput="../web/min/images"    # Directory where the 3XMM images must be copied 
iconsOutput="../web/min/icons" 
jsbasics="../web/saadajsbasics"    # Location of resources copied from the JSRESOURCE project
jsbasicsorg="/home/michel/workspace/jsresources/WebContent/saadajsbasics/"    # Location of resources in the JSRESOURCE project

#----------------------------------------------------------
# List of used jsresources JS objects
#
# MVC template for names:
#    Files without a js suffix are related to the MVC pattern.
#    There are actually 3 files *_m/v/c.js 

# Imported from JSRESOURCES/.../javascript
js_array_org=("basics.js" \
	                "WebSamp" \
	                "KWConstraint" \
	                "AttachedData_v.js" \
	                "VizierKeywords_v.js" \
	                 "OrderBy_v.js" \
	                "ConeSearch_v.js" \
	                "ConstList_v.js" \
	                "FieldList_v.js" \
	                "Sorter_v.js" \
	                "NodeFilter_v.js" \
	                "DataLink" \
	                "Segment.js" \
	                "RegionEditor" \
	                "ConstQEditor" \
	                "QueryTextEditor" \	  
	                "Pattern_v.js"	 \  
	                "domain.js" \
	                "HipsExplorer_v.js"	  
	                )
	                
# Imported from ../web/javascript
js_local_array=(	 
     "utils.js"  \
	 "dataTree.js"  \
	 "resultPaneModel.js"  \
     "resultPaneView.js"  \
     "resultPaneControler.js"  \
     "kwconstraintModel.js"  \
     "kwconstraintView.js"  \
     "kwconstraintControler.js"  \
     "saadaqlModel.js"  \
     "saadaqlView.js"  \
     "saadaqlControler.js" \
     "cartView.js"  \
     "cartControler.js"  \
     "cartModel.js"  \
     "sapView.js"  \
     "sapControler.js"  \
     "sapModel.js"  \
     "zipjobModel.js"  \
     "ready.js"  
     )

# Imported from .# Imported from JSRESOURCES/.../jsimports
js_import_array=(	 "jquery.simplemodal.js"  \
	 "jquery.alerts.js"  \
	 "jquery.dataTables.js"  \
     "aladin.js"  \
     "jquery.prints.js"  \
     "jquery.tooltip.js"  \
     "jquery.form.js" \
      "jquery.layout-latest.js"\
      "jquery.jstree.js"
      )

             
#
# Build the real list of jsresources JS files by applying the MVC template for names
#
js_basic_array=() 
for item in ${js_array_org[*]}
do
	if [[ "$item" == *.js ]]
	then
    	js_basic_array[${#js_basic_array[@]}]=$item	
	else
    	js_basic_array[${#js_basic_array[@]}]=$item'_m.js'
    	js_basic_array[${#js_basic_array[@]}]=$item'_v.js'
    	js_basic_array[${#js_basic_array[@]}]=$item'_c.js'
	fi
done

#---------------------------------------------------------------------------------------------
#  saadajsbasics  cleaning an update     
# 
echo "Clean min directory"
minDir=../web/min/
rm -Rf $minDir/*

echo "Create directories"
mkdir $minDir/jsimports 
mkdir $minDir/jsimports/ui 
mkdir $minDir/jsimports/ui/minified 

echo "Get JQUERY UI minified elements"
cp -R $jsbasicsorg/jsimports/ui/minified/* $minDir/jsimports/ui/minified

packedJS=$minDir/jsimports/packed.js

echo "Build $packedJS"
for item in ${js_import_array[@]}
	do
	    #cp $jsbasicsorg/jsimports/$item $minDir/jsimports		
	    cat $jsbasicsorg/jsimports/$item >> $packedJS || exit 1
		echo "" >> $packedJS
		echo "console.log('=============== > " $item "');" >> $packedJS
		echo "" >> $packedJS
	done

echo "get JSRESOURCES styles"
mkdir $minDir/styleimports 
mkdir $minDir/styleimports/themes 
mkdir $minDir/styleimports/themes/base 
mkdir $minDir/styleimports/themes/base/minified
cp -R  $jsbasicsorg/styleimports/themes/base/minified/* $minDir/styleimports/themes/base/minified
cp -R  $jsbasicsorg/styleimports/fonts                  $minDir/styleimports
cp -R  $jsbasicsorg/images/                             $minDir/
cp -R  $jsbasicsorg/icons                               $minDir/icons
cp -R  ../web/images/*                                  $minDir/images

cp -R $jsbasicsorg/styleimports/bootstrap      $minDir/styleimports/
cp -R $jsbasicsorg/styleimports/foundationicon $minDir/styleimports/

cp -R $jsbasicsorg/styleimports/layout-default-latest.css $minDir/styleimports/
cp -R $jsbasicsorg/styleimports/jquery.dataTables.css     $minDir/styleimports/
cp -R $jsbasicsorg/styleimports/simplemodal.css           $minDir/styleimports/
cp -R $jsbasicsorg/styleimports/aladin.min.css            $minDir/styleimports/

mkdir $minDir/styles 
cp $jsbasicsorg/styles/basics.css $minDir/styles
cp $jsbasicsorg/styles/domain.css $minDir/styles
cp ../web/styles/global.css       $minDir/styles
cp ../web/styles/form.css         $minDir/styles


mkdir $minDir/javascript 
packedJS=$minDir/javascript/packed.js
echo "Build $packedJS"

for item in ${js_basic_array[@]}
	do
	    #cp $jsbasicsorg/javascript/$item $minDir/javascript		
	    cat $jsbasicsorg/javascript/$item >> $packedJS || exit 1
		echo "" >> $packedJS
		echo "console.log('=============== > " $item "');" >> $packedJS
		echo "" >> $packedJS
	done
for item in ${js_local_array[@]}
	do
	    #cp ../web/javascript/$item $minDir/javascript
	    cat ../web/javascript/$item >> $packedJS || exit 1
		echo "" >> $packedJS
		echo "console.log('=============== > " $item "');" >> $packedJS
		echo "" >> $packedJS
	done
echo "Done"
exit 0

