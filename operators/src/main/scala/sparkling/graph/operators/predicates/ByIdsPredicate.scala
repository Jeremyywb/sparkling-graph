package sparkling.graph.operators.predicates

import org.apache.spark.graphx.VertexId
import sparkling.graph.api.operators.IterativeComputation.VertexPredicate

/**
 * Created by Roman Bartusiak (roman.bartusiak@pwr.edu.pl http://riomus.github.io).
 * Predicate for list of vertexIds
 */
case class ByIdsPredicate(vertex:List[VertexId]) extends VertexPredicate with Serializable {
  override def apply(v1: VertexId): Boolean = vertex.contains(v1)
}

