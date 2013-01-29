#!/bin/bash
# Licensed Materials - Property of IBM                              *
# eu.abc4trust.pabce.1.0                                            *
# (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
# US Government Users Restricted Rights - Use, duplication or       *
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
#********************************************************************

# Example usage:
# for i in $(find . -name "*.java"); do bash add_copyright.sh "$i"; done

set -e

EXPECTED_ARGS=1

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` {arg}"
  exit 1
fi

AUTH=$(mktemp)
TEMP=$(mktemp)
git log --format='%aN' -- "$1" | sort -u > $AUTH

# Remove old copyright
if grep '\/\/\*\/\*\*\/' "$1" -q; then
  # Delete everything before the magic string "//*/**/" if it is present
  sed '1,/\/\/\*\/\*\*\//d' "$1" -i
  # Also delete the blank line that comes afterwards
  sed '1d' "$1" -i
  echo "  !!! Removed old copyright notice"
fi


if grep "microsoft" "$AUTH" -q; then
  echo "MS  $1";
  cat - > "$TEMP" <<END
//* Licensed Materials - Property of IBM, Miracle A/S,                *
//* Alexandra Instituttet A/S, and Microsoft                          *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* (C) Copyright Microsoft Corp. 2012. All Rights Reserved.          *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

END
elif grep -v "zurich.ibm.com" "$AUTH" -q; then
  echo "COM $1";
  cat - > "$TEMP" <<END
//* Licensed Materials - Property of IBM, Miracle A/S, and            *
//* Alexandra Instituttet A/S                                         *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* (C) Copyright Miracle A/S, Denmark. 2012. All Rights Reserved.    *
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2012. All       *
//* Rights Reserved.                                                  *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

END
else
  echo "IBM $1";
  cat - > "$TEMP" <<END
//* Licensed Materials - Property of IBM                              *
//* eu.abc4trust.pabce.1.0                                            *
//* (C) Copyright IBM Corp. 2012. All Rights Reserved.                *
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*/**/****************************************************************

END
fi

cat "$1" >> "$TEMP"
mv "$TEMP" "$1"

rm "$AUTH"
