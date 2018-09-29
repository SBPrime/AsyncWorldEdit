#!/bin/bash

for i in $(find . -name *.pom | rev | cut -d'/' -f 3- | rev | uniq)
do
    versions=`find $i -name *.pom | rev | cut -d'/' -f2 | rev | sort`
    max=`echo $versions | rev  | cut -d' ' -f1 | rev`

    file=$i"/maven-metadata.xml"
    group=`echo $i | rev | cut -d'/' -f2- | rev | tr '/' '.' | cut -b3-`
    artifact=`echo $i | rev | cut -d'/' -f1 | rev`

    echo -e "<metadata>"						> $file
    echo -e "  <groupId>"$group"</groupId>"				>> $file
    echo -e "  <artifactId>"$artifact"</artifactId>"			>> $file
    echo -e "  <versioning>"						>> $file
    echo -e "    <release>"$max"</release>"				>> $file
    echo -e "    <versions>"						>> $file
    for j in $versions
    do
	echo -e "      <version>"$j"</version>"				>> $file
    done
    echo -e "    </versions>"						>> $file
    echo -e "    <lastUpdated>"$(date '+%Y%m%d%H%M%S')"</lastUpdated>"	>> $file
    echo -e "  </versioning>"						>> $file
    echo -e "</metadata>"						>> $file

    md5sum -t $file | cut -d' ' -f1 > $file.md5
    sha1sum -t $file | cut -d' ' -f1 > $file.sha1
done