#!/bin/bash
# Licensed Materials - Property of IBM                              *
# eu.abc4trust.pabce.1.0                                            *
# (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
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

AUTH="$(basename $0).$RANDOM.auth"
TEMP="$(basename $0).$RANDOM.temp"
git log --format='%aN (%ae)' -- "$1" | sort -u > $AUTH

# Remove old copyright
if grep '\/\/\*\/\*\*\/' "$1" -q; then
  # Delete everything before the magic string "//*/**/" if it is present
  sed '1,/\/\/\*\/\*\*\//d' "$1" -i
  # Also delete the blank line that comes afterwards
  # sed '1d' "$1" -i
  echo "  !!! Removed old copyright notice (1) $1"
elif grep '\/\/ \*\/\*\*\/' "$1" -q; then
  # Delete everything before the magic string "//*/**/" if it is present
  sed '1,/\/\/ \*\/\*\*\//d' "$1" -i
  echo "  !!! Removed old copyright notice (2) $1"
else
  echo "  NEW copyright copyright notice $1"
fi


cat - > "$TEMP" <<END
//* Licensed Materials - Property of                                  *
END

if grep "ibm" "$AUTH" -q; then
cat - >> "$TEMP" <<END
//* IBM                                                               *
END
fi
if grep "miracle" "$AUTH" -q; then
cat - >> "$TEMP" <<END
//* Miracle A/S                                                       *
END
fi
if grep "alexandra" "$AUTH" -q; then
cat - >> "$TEMP" <<END
//* Alexandra Instituttet A/S                                         *
END
fi
if grep "microsoft" "$AUTH" -q; then
cat - >> "$TEMP" <<END
//* Microsoft                                                         *
END
fi
if grep "nsn" "$AUTH" -q; then
cat - >> "$TEMP" <<END
//* Nokia                                                             *
END
fi

cat - >> "$TEMP" <<END
//*                                                                   *
//* eu.abc4trust.pabce.1.34                                           *
//*                                                                   *
END

if grep "ibm" "$AUTH" -q; then
cat - >> "$TEMP" <<END
//* (C) Copyright IBM Corp. 2014. All Rights Reserved.                *
END
fi
if grep "miracle" "$AUTH" -q; then
cat - >> "$TEMP" <<END
//* (C) Copyright Miracle A/S, Denmark. 2014. All Rights Reserved.    *
END
fi
if grep "alexandra" "$AUTH" -q; then
cat - >> "$TEMP" <<END
//* (C) Copyright Alexandra Instituttet A/S, Denmark. 2014. All       *
//* Rights Reserved.                                                  *
END
fi
if grep "microsoft" "$AUTH" -q; then
cat - >> "$TEMP" <<END
//* (C) Copyright Microsoft Corp. 2014. All Rights Reserved.          *
END
fi
if grep "nsn" "$AUTH" -q; then
cat - >> "$TEMP" <<END
//* (C) Copyright Nokia. 2014. All Rights Reserved.                   *
END
fi

cat - >> "$TEMP" <<END
//* US Government Users Restricted Rights - Use, duplication or       *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. *
//*                                                                   *
//* This file is licensed under the Apache License, Version 2.0 (the  *
//* "License"); you may not use this file except in compliance with   *
//* the License. You may obtain a copy of the License at:             *
//*   http://www.apache.org/licenses/LICENSE-2.0                      *
//* Unless required by applicable law or agreed to in writing,        *
//* software distributed under the License is distributed on an       *
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY            *
//* KIND, either express or implied.  See the License for the         *
//* specific language governing permissions and limitations           *
//* under the License.                                                *
//*/**/****************************************************************
END

cat "$1" >> "$TEMP"
mv "$TEMP" "$1"

rm "$AUTH"
