#!/bin/bash
set -e

OUTPUTDIR='/tmp/javadoc'
TARGETPACKAGE='eu'

echo "* Generating javadoc...";
javadoc -sourcepath src/main/java/ -d $OUTPUTDIR -nonavbar -subpackages $TARGETPACKAGE

echo "* Removing package comments...";
rm $(find $OUTPUTDIR -name package-frame.html)
rm $(find $OUTPUTDIR -name package-summary.html)
rm $(find $OUTPUTDIR -name package-tree.html)

echo "* Removing temporary xml classes...";
rm $(find $OUTPUTDIR/$TARGETPACKAGE -wholename "*/xml/*.html")

echo "* Removing temporary exceptions...";
rm $(find $OUTPUTDIR/$TARGETPACKAGE -wholename "*/exceptions/*.html")

echo "* Concatenating html files...";
cat $(find $OUTPUTDIR/$TARGETPACKAGE -name *.html) > $OUTPUTDIR/all.html

echo "* Removing title tag"
sed '/<TITLE>/,/<\/TITLE>/d' -i $OUTPUTDIR/all.html

echo "* Correcting the stylesheet"
sed 's/HREF=".*stylesheet.css"/HREF="stylesheet.css"/' -i $OUTPUTDIR/all.html

echo "* Converting html to postscript...";
(cd $OUTPUTDIR; html2ps -o api.ps -T -s 0.8 -d -U all.html)

echo "* Converting postscript to pdf...";
ps2pdf $OUTPUTDIR/api.ps  api.pdf

echo "* Done";
