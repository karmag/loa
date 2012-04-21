# Loa

Loa is a program that parses the information from
http://gatherer.wizards.com and produces XML formatted data.

## Quickstart

1. Get [leiningen](https://github.com/technomancy/leiningen)
2. lein deps
3. lein run --write

The output can be found in the data/zip directory.

## Details

### Options

    lein run --write

Produces zip-file with current date.

    lein run --debug --set mirrodin alara --card forest demon

`--debug` prints the internal data to the console. `--set` and
`--card` narrows the set of data to sets and cards that contain any of
the given strings.

### Program flow

1. Download set and card data.
2. Parse html into internal data format.
3. Write data as XML and text.
4. Create zip.

The program will download the english version of the cards from
gatherer. This is around 800Mb of data in some 20k files which means
it usually takes a while. The downloaded files are cached meaning that
the program can be terminated and restarted without losing to much
information.

### Structure

    data/
    |-- indata/
    |-- text/
    |-- tmp-download/
    |-- xml/
    '-- zip/

**indata** contains data that is not currently available in other
  form.

**text** is human-readable output. This file is generated from the
  XML-data and should not be used as a data-source.

**tmp-download** is where downloaded data is cached. This directory
  needs to be cleared if new data should be fetched from gatherer.

**xml** is where the canonical data is stored.

**zip** contains zip-files containing data from indata, text and xml
  directories. This is the deliverable.
