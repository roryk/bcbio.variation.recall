# bcbio.variation.recall

Parallel merging, squaring off and ensemble calling for genomic variants.
Provide a general framework meant to combine multiple variant calls, either from
single individuals, batched family calls, or multiple approaches on the same
sample. Splits inputs based on shared genomic regions without variants, allowing
independent processing of smaller regions with variant calls. Handles:

- Merging multiple samples, called independently, into a single final VCF file.
- Squaring off multiple samples, called independently, by recalling at all
  identified genomic positions.
- Ensemble calling for samples called with multiple variant callers. Uses an
  intersection based approach, selecting variants present in at least `n` callers,
  where you choose `n`.

This is a work in progress.

## Usage

The executable `bcbio-variation-recall` bash script contains a ready to run jar
file. Pre-built distributions will be available. To create a development version
run `make` and the executable will be available in the `bin` directory. This
requires [leiningen].

[leiningen]: http://leiningen.org/

### Merge

    Merge multiple VCF files together, running in parallel over genomic regions.

    Usage: bcbio-variation-recall merge [options] out-file ref-file vcf-files

      out-file:  VCF (or bgzipped VCF) file to write merged output to
      ref-file:  FASTA format genome reference file
      vcf-files: VCF files to merge. Can be specified on the command line
                 or as a text file containing paths to files for processing

    Options:
      -c, --cores CORES    1  Number of cores to use
      -r, --region REGION     Genomic region to subset, in samtools format (chr1:100-200) or BED file
      -h, --help

### Squaring off

    Perform squaring off for a set of called VCF files, recalling at no-call positions in each sample.

    Usage: bcbio-variation-recall square [options] out-file ref-file [<vcf, bam, cram, or list files>]

      out-file:    VCF (or bgzipped VCF) file to write merged output to
      ref-file:    FASTA format genome reference file
      <remaining>: VCF files to recall and BAM or CRAM files for each sample. Can be specified
                   on the command line or as text files containing paths to files
                   for processing. VCFs can be single or multi-sample and BAM/CRAMs can be in
                   any order but each VCF sample must have an associated BAM/CRAM file to recall.

    Options:
      -c, --cores CORES    1          Number of cores to use
      -m, --caller CALLER  freebayes  Calling method to use: samtools, freebayes, platypus
      -r, --region REGION             Genomic region to subset, in samtools format (chr1:100-200) or BED file
      -h, --help

### Ensemble

    Ensemble calling for samples: combine multiple VCF caller outputs into a single callset.

    Usage: bcbio-variation-recall ensemble [options] out-file ref-file [<vcf-files or list-files>]

       out-file:   VCF (or bgzipped VCF) file to write merged output to
       ref-file:   FASTA format genome reference file
      <remaining>: VCF files to include for building a final ensemble callset.
                   Specify on the command line or as text files containing paths to files.
                   VCFs can be single or multi-sample.
                   The input order of VCFs determines extraction preference in the final ensemble output.

    Options:
      -c, --cores CORES      1  Number of cores to use
      -n, --numpass NUMPASS  2  Number of callers a variant should be present in to pass
          --nofiltered          Remove filtered variants before performing ensemble calls
      -h, --help

## Thank you

External software provides the underlying algorithms. This tool is a framework
for pulling them together. The following command line programs need to be on
your path:

- [freebayes][freebayes]
- [glia][glia]
- [vcflib][vcflib]
- [GATK MIT licensed framework][gatk-framework]
- [vt]
- [bedtools][bedtools]
- [bcftools 1.x][bcftools]
- [samtools 1.x][samtools]
- [sambamba]
- [platypus]
- [scramble, from staden io_lib][scramble]

The [bcbio-nextgen][bcbio-nextgen] pipeline installs all this software automatically.

[bcbio-nextgen]: https://github.com/chapmanb/bcbio-nextgen
[bedtools]: http://bedtools.readthedocs.org/en/latest/
[vcflib]: https://github.com/ekg/vcflib
[bcftools]: https://github.com/samtools/bcftools
[freebayes]: https://github.com/ekg/freebayes
[gatk-framework]: https://github.com/chapmanb/gatk
[glia]: https://github.com/ekg/glia
[samtools]: http://samtools.sourceforge.net/
[platypus]: http://www.well.ox.ac.uk/platypus
[vt]: https://github.com/atks/vt
[scramble]: http://sourceforge.net/projects/staden/files/io_lib/
[sambamba]: https://github.com/lomereiter/sambamba

## License

The code is freely available under the [MIT license][l1].

[l1]: http://www.opensource.org/licenses/mit-license.html
