import java.io.{IOException, PrintWriter}
import org.apache.commons.io.IOUtils
import org.broadinstitute.sting.commandline.Input
import org.broadinstitute.sting.gatk.walkers.genotyper.GenotypeLikelihoodsCalculationModel
import org.broadinstitute.sting.queue.extensions.gatk.UnifiedGenotyper
import org.broadinstitute.sting.queue.QScript
import org.broadinstitute.sting.utils.exceptions.UserException
import scala.io.Source

class RunUGOnPON extends QScript{

    @Input(doc="Text file with a list of bamfiles to run UG on")
    var listOfBams: File = "/home/unix/louisb/cga_home/PanelOfNormalsAnalysis/bamfiles.txt"

    @Input(doc="Hotspots interval list")
    var hotspots: File ="/home/unix/louisb/cga_home/PanelOfNormalsAnalysis/hotspots.sorted.removedSexChrms.interval_list"

    @Input(doc="Reference fasta", required = false)
    var reference: File ="/seq/references/Homo_sapiens_assembly19/v1/Homo_sapiens_assembly19.fasta"

    @Argument(doc="output path", required = false)
    var outputdir: File = "."

    def readBamNamesFromFile(bamfiles: File) = {
        val file = Source.fromFile(bamfiles)
        file.getLines().flatMap{
            line => val bam = new File(line)
                if (bam.exists()) {
                    Some(bam)
                } else {
                    None
                }
        }
    }

    def writeBamNames(bamfiles: Seq[File], outputFile: File){
        var writer: PrintWriter = null
        try{
            writer = new PrintWriter(new File(outputFile,"outputList.txt"))
            bamfiles.foreach(file => writer.println(file.getAbsolutePath+ " " + swapExt(outputdir, file.getAbsolutePath,"bam","ug.vcf" ).getAbsolutePath() ) )
        } catch {
            case e: IOException => throw new UserException.CouldNotCreateOutputFile(outputFile, "Could not write bams file.",e)
        } finally {
            IOUtils.closeQuietly(writer)
        }
    }


    class GenotypeAtGivenSites(@Input val bam: File, @Input val intervalFile: File , @Input val outputDir: File) extends UnifiedGenotyper {
        input_file :+= bam
        reference_sequence = reference
        output_mode = org.broadinstitute.sting.gatk.walkers.genotyper.UnifiedGenotyperEngine.OUTPUT_MODE.EMIT_ALL_SITES
        intervals = Seq(intervalFile)
        out = swapExt(outputDir, bam, "bam","ug.vcf")
    }


    def script()= {

        val bamfiles = readBamNamesFromFile(listOfBams)

        writeBamNames(bamfiles.toSeq, outputdir)
        val genotypers = bamfiles.map( new GenotypeAtGivenSites(_, hotspots, outputdir))
        genotypers.foreach(add(_))

    }

}
