#!/usr/bin/python

import difflib
import logging
import math
import os
import shutil
import zipfile

import html

#--------------------------------------------------
#  data

class Package(object):
    def __init__(self, path):
        self.path = path
        self.dirpath = path.replace('.zip', '-dir')
        self.commentpath = path.replace('.zip', '.txt')

        self.date = path[9:19]
        self.prev = None

    def setup(self):
        logging.debug('Unzipping %s', self.path)
        if not os.path.exists(self.dirpath):
            zipfile.ZipFile(self.path).extractall(self.dirpath)

    def remove(self):
        logging.debug('Removing generated data for %s', self.path)
        if os.path.exists(self.dirpath):
            shutil.rmtree(self.dirpath)

    def comments(self):
        if os.path.exists(self.commentpath):
            return [line.strip()
                    for line in open(self.commentpath).readlines()
                    if line.strip()]
        logging.info('%s is missing comments.', self.path)
        return []

    def files(self):
        files = [os.path.join(base, file)
                 for base, dirs, files in os.walk(self.dirpath)
                 for file in files
                 if file.endswith('.txt') or file.endswith('.xml')]
        return [f for f in files if os.path.exists(f)]

packages = [Package(f)
            for f in os.listdir('.')
            if f.startswith('mtg-data-')
            if f.endswith('.zip')]

#--------------------------------------------------
#  other

def human_size(i, decimals=0):
    zeros = int(math.log10(i))
    index = zeros / 3

    div = float(1 if index == 0 else 10 ** (index * 3))

    fix = ["b", "kb", "Mb", "Gb"]

    fmt = "%%.%if&nbsp;%%s" % decimals
    return fmt % (i / div, fix[index])

#--------------------------------------------------
#  diff

def diff_path(path):
    return path + "-diff.html"

def gen_diff(pack):
    if not pack.prev:
        return {}

    mine = sorted(pack.files())
    other = sorted(pack.prev.files())

    ret = {}

    for a, b in zip(mine, other):
        logging.debug('Diff: %s --> %s', b, a)
        ret[a] = difflib.context_diff(open(b).readlines(),
                                      open(a).readlines(),
                                      fromfile=b,
                                      tofile=a)

    return ret

def make_diff_html(lines):
    root = html.XHTML('html')
    root.head().link(rel='stylesheet', href='../../loa-diff.css')
    body = root.body().pre()

    for line in lines:
        line = line.rstrip()
        if line.startswith('- ') or line == '-':
            body.span(klass='diff_removed').text(line)
        elif line.startswith('+ ') or line == '+':
            body.span(klass='diff_added').text(line)
        elif line.startswith('! ') or line == '!':
            body.span(klass='diff_altered').text(line)
        elif line.startswith('  '):
            body.span(klass='diff_context').text(line)
        else:
            body.span(klass='diff_heading').text(line)

        body.br()

    return root

def make_diff_page(pack):
    logging.debug('Diffing for %s', pack.path)
    diffs = gen_diff(pack)

    for key in sorted(diffs):
        if os.path.exists(diff_path(key)):
            continue

        lines = list(diffs[key])

        if not lines:
            continue

        logging.debug('Writing %s', diff_path(key))
        html = make_diff_html(lines)
        with open(diff_path(key), 'w') as f:
            f.write(str(html))

#--------------------------------------------------
#  index

def make_package_list(html, packs):
    table = html.table()

    tr = table.tr()
    tr.th('Link')
    tr.th('Date')
    tr.th('Updates')
    tr.th('Diffs')

    for p in sorted(packs, key=lambda p: p.date, reverse=True):
        tr = table.tr()

        dl = tr.td()
        dl.a('Get', href=p.path)
        dl.raw_text('&nbsp;[%s]' % human_size(os.path.getsize(p.path), 2))

        tr.td(p.date, style='width: 100px')

        comment = tr.td()
        for c in p.comments():
            comment.text("* " + c)
            comment.br()

        diff = tr.td()
        for f in p.files():
            if os.path.exists(diff_path(f)):
                size = os.path.getsize(diff_path(f))

                url = diff_path(f)
                name = "%s/%s" % (os.path.basename(os.path.dirname(f)),
                                  os.path.basename(f))
                diff.a(name, href=url)
                diff.raw_text('&nbsp;[%s]' % human_size(size))
                diff.br()

def make_index_html(packs):
    root = html.XHTML('html')
    root.head().link(rel='stylesheet', href='loa-diff.css')
    body = root.body()

    body.h1('Magic data')

    body.hr()

    make_package_list(body, packs)

    body.hr()

    intro = body.p()
    intro.text('Source code for all of this at ')
    intro.a('github', href='https://github.com/karmag/loa')
    intro.text('.')

    with open('index.html', 'w') as f:
        f.write(str(root))
        f.write('\n')

#--------------------------------------------------
#  main

def setup_prev_packages(packs):
    packs = sorted(packs, key=lambda p: p.date)
    for first, second in zip(packs, packs[1:]):
        second.prev = first

def run():
    setup_prev_packages(packages)

    for p in packages:
        #p.remove()
        p.setup()

    for p in packages:
        make_diff_page(p)

    make_index_html(packages)

if __name__ == '__main__':
    logging.basicConfig(level='DEBUG',
                        format='%(asctime)s <%(levelname)s> %(message)s',
                        datefmt='%H:%M:%S')
    run()
