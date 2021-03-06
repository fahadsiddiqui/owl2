package uk.co.turingatemyhamster
package owl2
package ast

/**
 *
 *
 * @author Matthew Pocock
 */
trait PropositionalConnectivesAndEnumerationOfIndividualsModuleAst extends owl2.PropositionalConnectivesAndEnumerationOfIndividualsModule {

  importedModules : owl2.IriModule with owl2.EntitiesLiteralsAnonymousIndividualsModule {
    type ClassExpression = ast.ClassExpression
  } =>

  

  override final type ObjectUnionOf = ast.ObjectUnionOf

  override final type ObjectComplementOf = ast.ObjectComplementOf

  override final type ObjectOneOf = ast.ObjectOneOf

  override final type ObjectIntersectionOf = ast.ObjectIntersectionOf

}

case class ObjectUnionOf(classExpressions: List[ClassExpression]) extends ClassExpression

case class ObjectComplementOf(classExpression: ClassExpression) extends ClassExpression

case class ObjectOneOf(individuals: List[Individual]) extends ClassExpression

case class ObjectIntersectionOf(classExpressions: List[ClassExpression]) extends ClassExpression