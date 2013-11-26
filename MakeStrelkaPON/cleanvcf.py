import sys

#remove lines that have a "." as part of one of the alt variants.
#this is a valid structural variant definition, but not supported by gatk.
#should not be present in strelka 1.0.11 or greater
def cleanFile(file):
    if not file.endswith(".vcf"):
        print("{file} isn't a vcf".format(file=file))
        return

    f = open(file,'r')
    clean = open(file.replace("vcf","clean.vcf") , 'w')
    for line in f:
        tokens = line.split("\t")
        if len(tokens) >= 4:
            if tokens[4].startswith('.') or tokens[4].endswith('.'):
                return 
        clean.write( "\t".join(tokens))
    return 
    
def main(argv):
    for arg in argv:
        cleanFile(arg)

if __name__ == '__main__':
    main(sys.argv[1:])


