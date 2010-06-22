#!/bin/bash

java -cp bin -Xmx1g edu.jhu.thrax.Thrax --grammar=hiero --source=example/europarl.es.small.1 --target=example/europarl.en.small.1 --alignment=example/es_en_europarl_alignments.txt.small.1 --debug

