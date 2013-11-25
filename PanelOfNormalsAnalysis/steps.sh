#find the germline bams that in the specified projects that are also in adam's list
grep -Ff projectList.txt kiezun_cancer_germline.bam.list > reduced_files.txt

#get the normal bams, not the reduced ones
sed 's/\.reduced//g' reduced_files.txt > bamfiles.txt

#convert mikes hotspot list into a format gatk will like
awk '{print $2":"$3};' pancan4700.v1f1.coding_only.hotspots.txt | skip > hotspots.interval_list

#Sort by chromosome and position
sort  -t: -k 1,1n -k 2,2n hotspots.interval_list > hotspots.sorted.interval_list

#Remove the sex chromosomes from the list
grep -v -E '^23|^24'  hotspots.sorted.interval_list > hotspots.sorted.removedSexChrms.interval_list

#manually remove junk at the end of the file
echo "Please delete the junk at the end of the file (or transpose it if it is real)"
