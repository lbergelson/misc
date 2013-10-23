/*
* Copyright (c) 2012 The Broad Institute
*
* Permission is hereby granted, free of charge, to any person
* obtaining a copy of this software and associated documentation
* files (the "Software"), to deal in the Software without
* restriction, including without limitation the rights to use,
* copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following
* conditions:
*
* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
* THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import org.broadinstitute.sting.queue.function.RetryMemoryLimit
import org.broadinstitute.sting.queue.QScript
import org.broadinstitute.sting.queue.util.Logging
import scala.io.Source

class MakeStrelkaPON extends QScript with Logging {

    @Input(doc="the runStrelka.sh script")
    var runStrelkaPath: File = _

    @Input(doc="a file containing a list of normal samples")
    var normals: File = _

    @Input(doc="reference file")
    var reference: File = _

    @Input(doc="output directory")
    var outputDir: File = _


    def script() {

        val source = Source.fromFile(normals)
        val lines = source.getLines().toList
        val normalFiles = lines.map(new File(_))
        val tumorFiles = Stream.continually(normalFiles).flatten.tail.take(normalFiles.length).toSeq

        (normalFiles, tumorFiles).zipped.foreach{ (norm, tum) =>
            val name = getName(norm,tum)
            add( ToolInvocation(runStrelkaPath,norm,tum,reference, name) )
        }

    }

    def getName(normal: File, tumor: File):String = {
         new File(outputDir, s"${normal.getName}-${tumor.getName}")
    }

    class ToolInvocation extends  CommandLineFunction with RetryMemoryLimit{
        @Input(doc="The script to run")
        var tool: File = _

        @Input(doc="normal sample bam")
        var normal: File = _

        @Input(doc="tumor sample bam")
        var tumor: File = _

        @Input(doc="reference fasta")
        var reference: File = _

        @Output(doc="output directory")
        var outputDir: File =_

        def commandLine = required(tool)+
            required(normal)+
            required(tumor)+
            required(reference)+
            required(outputDir)
    }

    object ToolInvocation {
        def apply(tool:File, normal:File, tumor:File, reference:File, outputDir:File) = {
            val ti = new ToolInvocation
            ti.tool = tool
            ti.normal = normal
            ti.tumor = tumor
            ti.reference = reference
            ti.outputDir = outputDir
            ti.memoryLimit = 4
            ti
        }
    }
}
