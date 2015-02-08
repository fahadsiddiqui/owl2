package uk.co.turingatemyhamster
package owl2
package examples

import ast._

import scala.language.implicitConversions

object UsingAst {

  implicit def stringToIRI(s: String): IRI = FullIRI(s)
  implicit def stringPairToIRI(ss: (String, String)): IRI = AbbreviatedIRI(prefixName = PrefixName(ss._1), abbreviatedString = ss._2)
  implicit def toClass[I](i: I)(implicit iIRI: I => IRI): Class = Class(iIRI(i))
  implicit def toDatatype[I](i: I)(implicit iIRI: I => IRI): Datatype = Datatype(i)
  implicit def toAnnotationProperty[I](i: I)(implicit iIRI: I => IRI): AnnotationProperty = AnnotationProperty(i)
  implicit def toNamedIndividual[I](i: I)(implicit iIRI: I => IRI): NamedIndividual = NamedIndividual(i)
  implicit def toObjectProperty[I](i: I)(implicit iIRI: I => IRI): ObjectProperty = ObjectProperty(i)
  implicit def toDataProperty[I](i: I)(implicit iIRI: I => IRI): DataProperty = DataProperty(i)

  implicit class Syntax[I](val _i: I) extends AnyVal {
    def ⊑ [J](j: J) = examples.⊑(_i, j)

    def is_a [J](j: J)(implicit iIndividual: I => Individual, jIRI: J => ClassExpression): ClassAssertion =
      ClassAssertion(Nil, _i, j)

    def --- [J](j: J) = new {
      def --> [K](k: K) = Triple(_i, j, k)
    }

    def inverse = Inverse(_i)

    def complement = Complement(_i)

    def ≤ (c: Int): ≤[I] = examples.≤(_i, BigInt(c))

    def === (c: Int): ===[I] = examples.===(_i, BigInt(c))

    def ≥ (c: Int): ≥[I] = examples.≥(_i, BigInt(c))

    def ^^[J](j: J)(implicit iString: I => String, jDatatype: J => Datatype): TypedLiteral = TypedLiteral(_i, j)

    def | [J](j: J)(implicit iDT: I => Datatype, jFR: J => FacetRestriction): DatatypeRestriction =
      DatatypeRestriction(datatype = _i, restrictions = j::Nil)
  }
}

case class ⊑ [Sub, Sup] (sub: Sub, sup: Sup)

object ⊑ {
  implicit def toSubClass[Sub, Sup](sc: ⊑[Sub, Sup])(implicit
                                                     ceSub: Sub => ClassExpression,
                                                     ceSup: Sup => ClassExpression): SubClassOf =
    SubClassOf(Nil, sc.sub, sc.sup)

  implicit def toSubObjectPropertyOf[Sub, Sup](sc: ⊑[Sub, Sup])(implicit
                                                                opeSub: Sub => ObjectPropertyExpression,
                                                                opeSup: Sup => ObjectPropertyExpression): SubObjectPropertyOf =
    SubObjectPropertyOf(Nil, sc.sub, (sc.sup : ObjectPropertyExpression) :: Nil)
}

case class Triple[Sub, Pred, Obj](sub: Sub, pred: Pred, obj: Obj)

object Triple {
  implicit def toAnnotation[Sub, Pred, Obj](t: Triple[Sub, Pred, Obj]
                                             )(implicit
                                               subSub: Sub => AnnotationSubject,
                                               predProp: Pred => AnnotationProperty,
                                               objVal: Obj => AnnotationValue): AnnotationAssertion =
    AnnotationAssertion(Nil, t.sub, t.pred, t.obj)

  implicit def toObjectPropertyAssertion[Sub, Pred, Obj](t: Triple[Sub, Pred, Obj]
                                                          )(implicit
                                                            subIndividual: Sub => Individual,
                                                            predPE: Pred => ObjectPropertyExpression,
                                                            objIndividual: Obj => Individual): ObjectPropertyAssertion =
    ObjectPropertyAssertion(Nil, t.sub, t.pred, t.obj)

  implicit def toDataPropertyAssertion[Sub, Pred, Obj](t: Triple[Sub, Pred, Obj]
                                                        )(implicit
                                                          subIndividual: Sub => Individual,
                                                          predPE: Pred => DataPropertyExpression,
                                                          objLit: Obj => Literal): DataPropertyAssertion =
    DataPropertyAssertion(Nil, t.sub, t.pred, t.obj)

}

case class Inverse[P](p: P) {
  def inverse: P = p
}

object Inverse {
  implicit def inverseOfP[P](i: Inverse[P])(implicit pP : P => ObjectProperty): InverseObjectProperty =
    InverseObjectProperty(i.p)
  implicit def inverseOfI(i: Inverse[InverseObjectProperty]): ObjectProperty =
    i.p.objectProperty
}

case class Complement[T](t: T) {
  def complement: T = t
}

object Complement {
  implicit def dataComplement[T](c: Complement[T])(implicit tD: T => DataRange): DataComplementOf =
    DataComplementOf(dataRange = c.t)

  implicit def objectComplement[T](c: Complement[T])(implicit cC: T => ClassExpression): ObjectComplementOf =
    ObjectComplementOf(classExpression = c.t)
}

case class ⊓ [I](i1: I, i2: I, is: List[I])

object ⊓ {
  def apply[I](i1: I, i2: I, is: I*): ⊓[I] = ⊓(i1, i2, is.to[List])

  implicit def objectIntersection[I](oi: ⊓ [I])(implicit iCE: I => ClassExpression): ObjectIntersectionOf =
    ObjectIntersectionOf(oi.i1 :: oi.i2 :: oi.is map iCE)
}

case class ⊔ [I](i1: I, i2: I, is: List[I])

object ⊔ {
  def apply[I](i1: I, i2: I, is: I*): ⊔[I] = ⊔(i1, i2, is.to[List])

  implicit def objectUnion[I](oi: ⊔ [I])(implicit iCE: I => ClassExpression): ObjectUnionOf =
    ObjectUnionOf(oi.i1 :: oi.i2 :: oi.is map iCE)
}

case class ≡[I](i1: I, i2: I, is: List[I])

object ≡ {
  def apply[I](i1: I, i2: I, is: I*): ≡ [I] = ≡(i1, i2, is.to[List])

  implicit def toEquivalentClasses[I](equiv: ≡[I])(implicit iClassExpression: I => ClassExpression): EquivalentClasses =
    EquivalentClasses(Nil, (equiv.i1 :: equiv.i2 :: equiv.is) map iClassExpression)
}

case class ≢[I](i1: I, i2: I, is: List[I])

object ≢ {
  def apply[I](i1: I, i2: I, is: I*): ≢[I] = ≢(i1, i2, is.to[List])

  implicit def toDifferentIndividuals[I](nequiv: ≢[I])(implicit iIndividual: I => Individual): DifferentIndividuals =
    DifferentIndividuals(Nil, (nequiv.i1 :: nequiv.i2 :: nequiv.is) map iIndividual)
}

case class ∋[I](i1: I, is: List[I])

object ∋ {
  def apply[I](i1: I, is: I*): ∋[I] = ∋(i1, is.to[List])

  implicit def toObjectOneOf[I](members: ∋[I])(implicit iIndividual: I => Individual): ObjectOneOf =
    ObjectOneOf((members.i1 :: members.is) map iIndividual)
}

case class ∃[P, V](p: P, v: V)

case object ∃ {
  implicit def someObjectSomeValuesFrom[P, V](some: ∃[P, V])(implicit
                                                             pPE: P => ObjectPropertyExpression,
                                                             vCE: V => ClassExpression): ObjectSomeValuesFrom =
    ObjectSomeValuesFrom(some.p, some.v)

  implicit def someDataSomeValuesFrom[P, V](some: ∃[P, V])(implicit
                                                           pPE: P => DataPropertyExpression,
                                                           vDR: V => DataRange): DataSomeValuesFrom =
    DataSomeValuesFrom(some.p::Nil, some.v)
}

case class ∀[P, V](p: P, v: V)

case object ∀ {
  implicit def allObjectValuesFrom[P, V](all: ∀[P, V])(implicit
                                                       pPE: P => ObjectPropertyExpression,
                                                       vCE: V => ClassExpression): ObjectAllValuesFrom =
    ObjectAllValuesFrom(all.p, all.v)

  implicit def allDataValuesFrom[P, V](all: ∀[P, V])(implicit
                                                     pPE: P => DataPropertyExpression,
                                                     vDR: V => DataRange): DataAllValuesFrom =
    DataAllValuesFrom(all.p::Nil, all.v)
}

case class ∈[P, V](p: P, v: V)

object ∈ {
  implicit def toObjectHasValue[P, V](pv: ∈[P, V])(implicit
                                                   pPE: P => ObjectPropertyExpression,
                                                   vI: V => Individual): ObjectHasValue =
    ObjectHasValue(pv.p, pv.v)

  implicit def toDataHasValue[P, V](pv: ∈[P, V])(implicit
                                                 pPE: P => DataPropertyExpression,
                                                 vI: V => Literal): DataHasValue =
    DataHasValue(pv.p, pv.v)
}

case class ≤[P](p: P, c: BigInt)

object ≤ {
  implicit def toObjectMaxCardinality[P](lteq: ≤[P])(implicit pPE: P => ObjectPropertyExpression): ObjectMaxCardinality =
    ObjectMaxCardinality(lteq.p, None, lteq.c)
}

case class ===[P](p: P, c: BigInt)

object === {
  implicit def toObjectExactCardinality[P](eq: ===[P])(implicit pPE: P => ObjectPropertyExpression): ObjectExactCardinality =
    ObjectExactCardinality(eq.p, None, eq.c)
}

case class ≥[P](p: P, c: BigInt)

object ≥ {
  implicit def toObjectMaxCardinality[P](gteq: ≥[P])(implicit pPE: P => ObjectPropertyExpression): ObjectMinCardinality =
    ObjectMinCardinality(gteq.p, None, gteq.c)
}


/**
 *
 *
 * @author Matthew Pocock
 */
class UsingAst {

  import UsingAst._

  Ontology(
    directlyImportsDocuments = "http://www.example.com/ontolgy1#"::Nil,
    ontologyAnnotations = Annotation(annotationProperty = AnnotationProperty("rdfs" -> "label"),
      annotationValue = StringLiteralNoLangauge("An example")) :: Nil,
    axioms = SubClassOf(subClassExpression = "a" -> "Child", superClassExpression = "a" -> "Person") :: Nil)

  Ontology(
    directlyImportsDocuments = FullIRI("http://www.example.com/2.0")::Nil,
    ontologyIRI = Some(FullIRI("http://www.example.com/importing-ontology")))

  val aChild1 = Class(entityIRI = AbbreviatedIRI(prefixName = PrefixName("a"), abbreviatedString = "Child"))
  val aChild2 = Class("a" -> "child")
  val aChild3 = "a" -> "child" : Class

  val aPerson1 = "a" -> "person" : Class

  SubClassOf(
    subClassExpression = Class(AbbreviatedIRI(PrefixName("a"), "Child")),
    superClassExpression = Class(AbbreviatedIRI(PrefixName("a"), "Person")))

  SubClassOf(
    subClassExpression = "a" -> "Child",
    superClassExpression = "a" -> "Person")

  SubClassOf(Nil, "a" -> "Child", "a" -> "Person")

  ⊑(aChild1, aPerson1)
  ⊑(aChild1, aPerson1) : SubClassOf
  aChild1 ⊑ aPerson1
  aChild1 ⊑ aPerson1 : SubClassOf

  ("a" -> "Child") ⊑ ("a" -> "Person") : SubClassOf
  ("a" -> "Child") ⊑ ("a" -> "Person") : ClassAxiom

  val xsdInteger1 = "xsd" -> "integer"
  val xsdInteger2 = "xsd" -> "integer" : Datatype
  val xsdInteter3 = "xsd" -> "integer" : DataRange

  DataPropertyRange(Nil, "a" -> "hasAge", "xsd" -> "integer")

  Declaration(Nil, Class("owl" -> "Thing"))
  Declaration(Nil, Class("owl" -> "Nothing"))
  Declaration(Nil, ObjectProperty("owl" -> "topObjectProperty"))
  Declaration(Nil, ObjectProperty("owl" -> "bottomObjectProperty"))
  Declaration(Nil, DataProperty("owl" -> "topDataProperty"))
  Declaration(Nil, DataProperty("owl" -> "bottomDataProperty"))
  Declaration(Nil, Datatype("rdfs" -> "Literal"))

  ClassAssertion(Nil, "a" -> "Dog", "a" -> "Brian")
  ClassAssertion(Nil, "a" -> "Species", "a" -> "Dog")
  ClassAssertion(Nil, "a" -> "PetAnimal", "a" -> "Dog")

  "a" -> "Brian" is_a "a" -> "Dog"
  "a" -> "Dog" is_a "a" -> "species"
  "a" -> "Dog" is_a "a" -> "PetAnimal"
  
  AnnotationAssertion(Nil, "a" -> "Dog", "a" -> "addedBy", "Seth MacFarlane")
  ("a" -> "Dog") --- ("a" -> "addedBy") --> "Seth MacFarlane" : AnnotationAssertion

  ObjectPropertyAssertion(Nil, "a" -> "Peter", "a" -> "fatherOf", "a" -> "Stewie")
  ("a" -> "Peter") --- ("a" -> "fatherOf") --> ("a" -> "Stewie") : ObjectPropertyAssertion

  InverseObjectProperty("a" -> "fatherOf")
  ("a" -> "fatherOf").inverse : InverseObjectProperty
  (("a" -> "fatherOf").inverse : InverseObjectProperty).inverse : ObjectProperty
  ("a" -> "fatherOf").inverse.inverse : ObjectProperty

  DataIntersectionOf(dataRanges =
    ("xsd" -> "nonNegativeInteger" : DataRange) ::
      ("xsd" -> "nonPositiveInteger" : DataRange) ::
      Nil)

  DataUnionOf(dataRanges =
    ("xsd" -> "string" : DataRange) ::
      ("xsd" -> "integer" : DataRange) ::
      Nil)

  DataComplementOf(dataRange = "xsd" -> "positiveInteger")
  ("xsd" -> "positiveInteger").complement : DataComplementOf

  ObjectIntersectionOf(("a" -> "Dog" : ClassExpression) :: ("a" -> "CanTalk" : ClassExpression) :: Nil)
  ⊓("a" -> "Dog", "a" -> "CanTalk") : ObjectIntersectionOf

  ObjectUnionOf(("a" -> "Man" : ClassExpression) :: ("a" -> "Woman" : ClassExpression) :: Nil)
  ⊔("a" -> "Man", "a" -> "Woman") : ObjectUnionOf

  ObjectComplementOf("a" -> "man")
  ("a" -> "man").complement : ObjectComplementOf
  ("a" -> "man").complement.complement : ClassExpression

  EquivalentClasses(Nil, List("a" -> "GriffinFamilyMember" : ClassExpression, ObjectOneOf(
    List("a" -> "Peter" : Individual,
    "a" -> "Lois",
    "a" -> "Stewie",
    "a" -> "Meg",
    "a" -> "Chris",
    "a" -> "Brian")
  )))
  DifferentIndividuals(individuals = List("a" -> "Quagmire" : Individual,
    "a" -> "Peter",
    "a" -> "Lois",
    "a" -> "Stewie",
    "a" -> "Meg",
    "a" -> "Chris",
    "a" -> "Brian")
  )
  ("a" -> "GriffinFamilyMember").complement : ClassExpression

  ≡("a" -> "GriffinFamilyMember" : ClassExpression,
    ∋(
      "a" -> "Peter",
      "a" -> "Lois",
      "a" -> "Stewie",
      "a" -> "Meg",
      "a" -> "Chris",
      "a" -> "Brian") : ClassExpression) : ClassAxiom
  ≢("a" -> "Quagmire",
    "a" -> "Peter",
    "a" -> "Lois",
    "a" -> "Stewie",
    "a" -> "Meg",
    "a" -> "Chris",
    "a" -> "Brian")


  ObjectPropertyAssertion(Nil, "a" -> "Peter", "a" -> "fatherOf", "a" -> "Stewie")
  ClassAssertion(Nil, "a" -> "Stewie", "a" -> "Man")

  ("a" -> "Peter") --- ("a" -> "fatherOf") --> ("a" -> "Stewie")
  "a" -> "Stewie" is_a "a" -> "Man"

  ObjectSomeValuesFrom("a" -> "fatherOf", "a" -> "Man")
  ∃("a" -> "fatherOf", "a" -> "Man") : ObjectSomeValuesFrom

  ObjectAllValuesFrom("a" -> "hasPet", "a" -> "Dog")
  ∀("a" -> "hasPet", "a" -> "Dog") : ObjectAllValuesFrom

  ObjectHasValue("a" -> "hasPet", "a" -> "Brian")
  ∈("a" -> "hasPet", "a" -> "Brian") : ObjectHasValue

  ObjectHasSelf("a" -> "likes")

  ObjectMaxCardinality(objectPropertyExpression = "a" -> "hasPet", cardinality = BigInt(1))
  ("a" -> "Peter") is_a ("a" -> "hasPet" ≤ 1)

  ObjectExactCardinality(objectPropertyExpression = "a" -> "hasPet", cardinality = BigInt(1))
  ("a" -> "hasPet") === 1 : ObjectExactCardinality

  ObjectMinCardinality(objectPropertyExpression = "a" -> "hasPet", cardinality = BigInt(1))
  "a" -> "hasPet" ≥ 1 : ObjectMinCardinality

  DataPropertyAssertion(Nil, "a" -> "Meg", "a" -> "hasAge", "17"^^("xsd" -> "integer"))
  ("a" -> "Meg") --- ("a" -> "hasAge") --> ("17"^^("xsd" -> "integer")) : DataPropertyAssertion

  DataSomeValuesFrom(
    ("a" -> "hasAge")::Nil,
    DatatypeRestriction(
      BigInt(1),
      "xsd" -> "integer",
      FacetRestriction("xsd" -> "maxExclusive", "20"^^("xsd" -> "integer"))::Nil),
    BigInt(1))

  ∃("a" -> "hasAge", "xsd" -> "integer" | "xsd" -> "maxExclusive" -> ("20"^^("xsd" -> "integer"))) : DataSomeValuesFrom
  ∀("a" -> "hasZIP", "xsd" -> "integer") : DataAllValuesFrom
  ∈("a" -> "hasAge", "17"^^("xsd" -> "integer")) : DataHasValue

  SubObjectPropertyOf(Nil, "a" -> "hasDog", ("a" -> "hasPet" : ObjectPropertyExpression) :: Nil)
  ObjectPropertyAssertion(Nil, "a" -> "Peter", "a" -> "hasDog", "a" -> "Brian")
  ObjectPropertyAssertion(Nil, "a" -> "Peter", "a" -> "hasPet", "a" -> "Brian")

  ("a" -> "hasDog") ⊑ ("a" -> "hasPet") : SubObjectPropertyOf
  ("a" -> "Peter") --- ("a" -> "hasDog") --> ("a" -> "Brian")
  ("a" -> "Peter") --- ("a" -> "hasPet") --> ("a" -> "Brian")


}
