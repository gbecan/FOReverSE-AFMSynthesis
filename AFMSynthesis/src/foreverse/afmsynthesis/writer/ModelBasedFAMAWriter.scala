package foreverse.afmsynthesis.writer

import java.io.File
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.AttributedFeature
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.FAMAAttributedFeatureModel
import es.us.isa.FAMA.models.domain.SetIntegerDomain
import es.us.isa.FAMA.models.featureModel.Cardinality
import es.us.isa.FAMA.models.featureModel.extended.GenericAttribute
import foreverse.afmsynthesis.afm.AttributedFeatureModel
import foreverse.afmsynthesis.afm.Feature
import foreverse.afmsynthesis.afm.Mandatory
import foreverse.afmsynthesis.afm.Mandatory
import foreverse.afmsynthesis.afm.MutexGroup
import foreverse.afmsynthesis.afm.Optional
import foreverse.afmsynthesis.afm.OrGroup
import foreverse.afmsynthesis.afm.Relation
import foreverse.afmsynthesis.afm.XorGroup
import gsd.graph.ImplicationGraph
import scala.collection.JavaConversions._
import java.util.HashSet
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.RequiresDependency
import foreverse.afmsynthesis.afm.BinaryImplicationConstraint
import foreverse.afmsynthesis.afm.AttributedFeatureDiagram
import foreverse.afmsynthesis.afm.BinaryExclusionConstraint
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.ExcludesDependency

class ModelBasedFAMAWriter extends FAMAWriter {

  override def write(afm : AttributedFeatureModel, file : File) {
	  val famaAFM = new FAMAAttributedFeatureModel

	  // Create FAMA features
	  val afd = afm.diagram
	  val afmToFAMA = afd.features.map(f => (f -> new AttributedFeature)).toMap
	  for ((feature, famaFeature) <- afmToFAMA) {
	    famaFeature.setName(feature.name)
	  }
	  
	  writeHierarchy(afd, famaAFM, afmToFAMA)
	  writeAttributes(afd, famaAFM, afmToFAMA)
	  writeConstraints(afd, famaAFM, afmToFAMA)
	  
	  val writer = new AttributedWriter
	  file.delete()
	  writer.writeFile(file.getAbsolutePath(), famaAFM)
  }
  
  private def writeHierarchy(afd : AttributedFeatureDiagram, famaAFM : FAMAAttributedFeatureModel, afmToFAMA : Map[Feature, AttributedFeature]) {
    
    val hierarchy = afd.hierarchy
    val relations = afd.mandatoryRelations ::: 
				afd.mutexGroups ::: 
				afd.orGroups ::: 
				afd.xorGroups
    val roots = hierarchy.roots()
    assert(roots.size() == 1, "an AFM must have exactly one root")
    
    val root = roots.head
     
    // Set root
    val famaRoot = afmToFAMA(root)
    famaAFM.setRoot(famaRoot)
    
    relations.foreach(println)
    
    // Set relations
    for (relation <- relations) {
      val famaRelation = new es.us.isa.FAMA.models.FAMAAttributedfeatureModel.Relation
      afmToFAMA(relation.parent).addRelation(famaRelation)
      relation.children.foreach(c => famaRelation.addDestination(afmToFAMA(c))) 
      
      val (cardInf, cardSup) = relation match {
        case Optional(_,_) => (0,0)
        case Mandatory(_,_) => (1, 1)
        case MutexGroup(_,_) => (0, 1)
        case OrGroup(_,_) => (1, relation.children.size)
        case XorGroup(_,_) => (1, 1) // FIXME : not working
      }
      println(relation.parent + " / " + relation.children + " : " + cardInf + ", " + cardSup)
      famaRelation.addCardinality(new Cardinality(cardInf, cardSup))
    }
    
    // Set optional relations if there were not already added
    for ((feature, famaFeature) <- afmToFAMA) {
      if ((feature != root) && (!relations.exists(_.children.contains(feature)))) {
        val parent = hierarchy.parents(feature).head
        val famaParent = afmToFAMA(parent)
        
        val famaRelation = new es.us.isa.FAMA.models.FAMAAttributedfeatureModel.Relation
        famaRelation.addDestination(famaFeature)
        famaParent.addRelation(famaRelation)
        famaRelation.addCardinality(new Cardinality(0, 1))
        
      }
    }
    

     
  }
  
  private def writeAttributes(afd : AttributedFeatureDiagram, famaAFM : FAMAAttributedFeatureModel, afmToFAMA : Map[Feature, AttributedFeature]) {
    for ((feature, famaFeature) <- afmToFAMA) {
    	for (attribute <- feature.attributes) {
    	  val name = attribute.name
    	  
    	  val intValues : java.util.Set[Integer] = new HashSet[Integer]
    	  attribute.domain.values.foreach(v => intValues.add(v.toInt))
    	  val domain = new SetIntegerDomain(intValues)
    	  
    	  val nullValue = attribute.domain.nullValue.toInt
    	  val defaultValue = attribute.domain.values.head.toInt
    	  
    	  val famaAttribute = new GenericAttribute(attribute.name, domain, nullValue, defaultValue)
    	  
    	  famaFeature.addAttribute(famaAttribute)
    	}
    }
  }
  
  private def writeConstraints(afd : AttributedFeatureDiagram, famaAFM : FAMAAttributedFeatureModel, afmToFAMA : Map[Feature, AttributedFeature]) {

    for (constraint <- afd.constraints) {
      val famaConstraint = constraint match {
        case BinaryImplicationConstraint(feature, implied) => 
          Some(new RequiresDependency(afmToFAMA(feature), afmToFAMA(implied)))
        case BinaryExclusionConstraint(feature, excluded) =>
          Some(new ExcludesDependency(afmToFAMA(feature), afmToFAMA(excluded)))
        case _ => None
      }
      
      if (famaConstraint.isDefined) {
    	  famaAFM.addConstraint(famaConstraint.get)
      }
    }
  }
  
  
}