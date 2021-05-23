#!/bin/sh
alias pksi='java -cp "<path to picocli jar>:<path to service initializer jar>" com.pk.si.PKSI'
pksi soap -w <path to wsdl> -o <path to output folder>

