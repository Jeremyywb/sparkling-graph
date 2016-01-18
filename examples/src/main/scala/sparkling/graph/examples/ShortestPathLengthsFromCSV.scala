package sparkling.graph.examples

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.graphx.{VertexId, Graph, PartitionStrategy}
import org.apache.spark.{SparkConf, SparkContext}
import sparkling.graph.api.operators.algorithms.shortestpaths.ShortestPathsTypes._
import sparkling.graph.loaders.csv.{CSVLoader, CsvLoaderConfig}
import sparkling.graph.operators.algorithms.shortestpaths.ShortestPathsAlgorithm
import sparkling.graph.operators.algorithms.shortestpaths.pathprocessors.fastutils.FastUtilWithDistance._
import sparkling.graph.operators.predicates.{AllPathPredicate, ByIdsPredicate}
import scala.collection.JavaConversions._
/**
 * Created by Roman Bartusiak (roman.bartusiak@pwr.edu.pl http://riomus.github.io).
 */
object ShortestPathLengthsFromCSV extends ExampleApp {
def body()={
  val shortestPaths =if(bucketSize == -1l)
    ShortestPathsAlgorithm.computeShortestPathsLengths(partitionedGraph,AllPathPredicate,treatAsUndirected)
  else
    ShortestPathsAlgorithm.computeShortestPathsLengthsIterative(partitionedGraph,(g:Graph[_,_])=>bucketSize,treatAsUndirected)
  val size: Broadcast[VertexId] =ctx.broadcast(partitionedGraph.numVertices)
  partitionedGraph.outerJoinVertices(shortestPaths.vertices)(Util.dataTransformFunction(size) _).vertices.values.saveAsTextFile(out)
  ctx.stop()
}
}


private object Util{
  def dataTransformFunction(size: Broadcast[VertexId])(vId: VertexId,oldValue: String,pathsOption: Option[_ >: DataMap <: JMap[JLong, JDouble]])={
    pathsOption.flatMap((paths)=>{
      var entries=paths.entrySet().toList.sortBy(_.getKey)
      val out=new StringBuilder()
      out++=s"${oldValue},"
      var a = 0l
      while (a < size.value) {
        if (entries.size > 0 && a == entries.head.getKey) {
          out ++= s"${entries.head.getValue},"
          entries = entries.drop(1)
        }
        else {
          out ++= "0,"
        }
        a += 1l
      }
      out.setLength(out.length - 1)
      Option(out.toString())
    }).getOrElse(oldValue)
  }
}