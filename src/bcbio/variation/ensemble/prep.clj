(ns bcbio.variation.ensemble.prep
  "Prepare variant inputs for ensemble calling approaches.
   Creates normalized, bgzipped tabix indexed inputs from VCF files."
  (:require [bcbio.run.fsp :as fsp]
            [bcbio.run.itx :as itx]
            [clojure.core.strint :refer [<<]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [me.raynes.fs :as fs]))

(defn- tabix-index-vcf
  "Tabix index input VCF inside a transactional directory."
  [bgzip-file]
  (let [tabix-file (str bgzip-file ".tbi")]
    (when (itx/needs-run? tabix-file)
      (itx/with-tx-file [tx-tabix-file tabix-file]
        (let [tx-bgzip-file (fsp/file-root tx-tabix-file)
              full-bgzip-file (str (fs/file bgzip-file))
              tmp-dir (str (fs/parent tx-bgzip-file))]
          (itx/check-run (<< "ln -s ~{full-bgzip-file} ~{tx-bgzip-file}"))
          (itx/check-run (<< "bcftools tabix -p vcf ~{tx-bgzip-file}")))))
    tabix-file))

(defn bgzip-index-vcf
  "Prepare a VCF file for positional query with bgzip and tabix indexing."
  [vcf-file & {:keys [remove-orig?]}]
  (let [out-file (if (.endsWith vcf-file ".gz") vcf-file (str vcf-file ".gz"))]
    (itx/run-cmd out-file "bgzip -c ~{vcf-file} > ~{out-file}")
    (when remove-orig?
      (fsp/remove-path vcf-file))
    (tabix-index-vcf out-file)
    out-file))

(defn region->samstr
  [region]
  (format "%s:%s-%s" (:chrom region) (inc (:start region)) (:end region)))

(defn region->safestr
  [region]
  (format "%s_%s_%s" (:chrom region) (:start region) (:end region)))

(defn norm-bgzip
  "Normalize and bgzip/tabix index a VCF input file in a defined region."
  [vcf-file region out-dir]
  (let [prep-vcf-file (bgzip-index-vcf vcf-file)
        out-file (str (io/file out-dir (str (fs/base-name vcf-file) ".gz")))]
    (itx/run-cmd out-file
             "bcftools subset -r ~{(region->samstr region)} ~{prep-vcf-file} | "
             "vcfallelicprimitives | "
             "bgzip -c /dev/stdin > ~{out-file}")
    (tabix-index-vcf out-file)
    out-file))

(defn create-union
  "Create a union file with inputs from multiple variant callers."
  [vcf-files ref-file region out-dir]
  (let [out-file (str (io/file out-dir (str "union-" (region->safestr region) ".vcf")))
        intersect-str (string/join " | " (map (fn [x] (<< "vcfintersect -r ~{ref-file} -u ~{x}"))
                                              (rest vcf-files)))]
    (itx/run-cmd out-file
             "zcat ~{(first vcf-files)} | ~{intersect-str} | "
             "vcfcreatemulti > ~{out-file}")
    (bgzip-index-vcf out-file)))
