# Loa

Loa is a program that parses the information from
http://gatherer.wizards.com and produces XML formatted data.

# Quickstart

1. Get [leiningen](https://github.com/technomancy/leiningen)
2. Setup dependencies (see below)
3. `lein run --full`

The output can be found in the data/zip directory.

## Dependency

1. Get [ants](https://github.com/karmag/ants)
2. lein install

# Details

## Options

    lein run --full
    lein run --meta --language --package

`--full` and `--meta --language --package` are equivalent. Produces
zip-file with current date.

    lein run --full --threads 4

The number of threads default to the number of cores available on your
machine but can be overridden with `--threads`.

    lein run --debug --set mirrodin alara --card forest demon

`--debug` prints the internal data to the console. `--set` and
`--card` narrows the set of data to sets and cards whose names contain
any of the given strings.

## Structure

### Source

    src/
    '-- loa
        |-- cleanup
        |-- format
        |-- gatherer
        |-- indata
        |-- program
        |-- util
        '-- validation

**cleanup** has functions and data for processing "raw" cards (from
  gatherer). Also contains information about cards not in gatherer.

**format** is for transforming the internal data represenation to xml.

**gatherer** functions for parsing gatherer html pages.

**indata** processing of data that is not fetched from gatherer.

**program** functions that combine the other parts into a proper
  application.

**util** non-application specific functionality.

**validation** functions for data validation.

### Data

    data/
    |-- indata/
    |-- tmp-download/
    |-- xml/
    '-- zip/

**indata** contains data that is not currently available in other
  form.

**tmp-download** is where downloaded data is cached. This directory
  needs to be cleared if new data should be fetched from gatherer.

**xml** is where the canonical data is stored.

**zip** contains zip-files containing data from indata, text and xml
  directories. This is the deliverable.
