package projects.izi.leipzigLocal

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.enums.SampleKind

import static de.kairos.fhir.centraxx.metamodel.AbstractEntity.ID
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.COLD_ISCH_TIME
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.COLD_ISCH_TIME_DATE
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.ID_CONTAINER
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.PARENT
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.SAMPLE_CATEGORY
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.SAMPLE_KIND
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.SECOND_PROCESSING
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.SECOND_PROCESSING_DATE
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.SPREC_FIXATION_TIME
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.SPREC_FIXATION_TIME_DATE
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.SPREC_POST_CENTRIFUGATION_DELAY
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.SPREC_POST_CENTRIFUGATION_DELAY_DATE
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.SPREC_PRE_CENTRIFUGATION_DELAY
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.SPREC_PRE_CENTRIFUGATION_DELAY_DATE
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.SPREC_PRIMARY_SAMPLE_CONTAINER
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.SPREC_TISSUE_COLLECTION_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.STOCK_PROCESSING
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.STOCK_PROCESSING_DATE
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.STOCK_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.USE_SPREC
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.WARM_ISCH_TIME
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.WARM_ISCH_TIME_DATE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * Represented by a CXX AbstractSample
 * @author Mike Wähnert
 * @since v.1.7.0, CXX.v.3.17.2
 */
specimen {

  id = "Specimen/" + context.source[ID]

  final def idContainer = context.source[ID_CONTAINER]?.find {
    "SAMPLEID" == it[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  if (idContainer) {
    identifier {
      value = idContainer[PSN]
      type {
        coding {
          system = "urn:centraxx"
          code = idContainer[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
        }
      }
    }
  }

  final def idSampleQf = context.source[ID_CONTAINER]?.find {
    "SAMPLEQF" == it[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  if (idSampleQf) {
    identifier {
      value = idSampleQf[PSN]
      type {
        coding {
          system = "urn:centraxx"
          code = idSampleQf[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
        }
      }
    }
  }

  status = context.source[sample().restAmount().amount()] > 0 ? "available" : "unavailable"

  type {
    coding {
      system = "urn:centraxx"
      code = context.source[sample().sampleType().code()]
    }
  }

  final def patIdContainer = context.source[sample().patientContainer().idContainer()]?.find {
    "SID" == it[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  if (patIdContainer) {
    subject {
      identifier {
        value = patIdContainer[PSN]
        type {
          coding {
            system = "urn:centraxx"
            code = patIdContainer[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
          }
        }
      }
    }
  }

  if (context.source[PARENT] != null) {
    parent {
      reference = "Specimen/" + context.source[sample().parent().id()]
    }
  }

  receivedTime {
    date = context.source[sample().receiptDate().date()]
  }

  collection {
    collectedDateTime {
      date = context.source[sample().samplingDate().date()]
      quantity {
        value = context.source[sample().initialAmount().amount()] as Number
        unit = context.source[sample().initialAmount().unit()]
        system = "urn:centraxx"
      }
    }
  }

  container {
    if (context.source[sample().receptable()]) {
      identifier {
        value = context.source[sample().receptable().code()]
        system = "urn:centraxx"
      }

      capacity {
        value = context.source[sample().receptable().size()]
        unit = context.source[sample().restAmount().unit()]
        system = "urn:centraxx"
      }
    }

    specimenQuantity {
      value = context.source[sample().restAmount().amount()] as Number
      unit = context.source[sample().restAmount().unit()]
      system = "urn:centraxx"
    }
  }

  extension {
    url = FhirUrls.Extension.SAMPLE_CATEGORY
    valueCoding {
      system = "urn:centraxx"
      code = context.source[SAMPLE_CATEGORY]
    }
  }

  if (context.source[sample().repositionDate()]) {
    extension {
      url = FhirUrls.Extension.Sample.REPOSITION_DATE
      valueDateTime = context.source[sample().repositionDate().date()]
    }
  }

  if (context.source[sample().derivalDate()]) {
    extension {
      url = FhirUrls.Extension.Sample.DERIVAL_DATE
      valueDateTime = context.source[sample().derivalDate().date()]
    }
  }

  if (context.source[sample().concentration()]) {
    extension {
      url = FhirUrls.Extension.Sample.CONCENTRATION
      valueQuantity {
        value = context.source[sample().concentration().amount()]
        unit = context.source[sample().concentration().unit()]
      }
    }
  }

  // Sample Location
  if (context.source[sample().sampleLocation()]) {
    extension {
      url = "https://fhir.centraxx.de/extension/sample/sampleLocation"
      extension {
        url = "https://fhir.centraxx.de/extension/sample/sampleLocationPath"
        valueString = context.source[sample().sampleLocation().locationPath()]
      }
      final Integer xPos = context.source[sample().xPosition()] as Integer
      if (xPos) { // necessary, because groovy interprets 0 to false
        extension {
          url = "https://fhir.centraxx.de/extension/sample/xPosition"
          valueInteger = xPos
        }
      }
      final Integer yPos = context.source[sample().yPosition()] as Integer
      if (yPos) {
        extension {
          url = "https://fhir.centraxx.de/extension/sample/yPosition"
          valueInteger = yPos
        }
      }
    }
  }

  if (context.source[sample().organisationUnit()]) {
    extension {
      url = FhirUrls.Extension.Sample.ORGANIZATION_UNIT
      valueReference {
        identifier {
          value = context.source[sample().organisationUnit().code()] as String
        }
      }
    }
  }

  // SPREC Extensions
  extension {
    url = FhirUrls.Extension.SPREC
    extension {
      url = FhirUrls.Extension.Sprec.USE_SPREC
      valueBoolean = context.source[USE_SPREC]
    }

    //
    // SPREC TISSUE
    //
    if (SampleKind.TISSUE == context.source[SAMPLE_KIND] as SampleKind) {
      if (context.source[SPREC_TISSUE_COLLECTION_TYPE]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_TISSUE_COLLECTION_TYPE
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().sprecTissueCollectionType().code()]
          }
        }
      }
      if (context.source[WARM_ISCH_TIME]) {
        extension {
          url = FhirUrls.Extension.Sprec.WARM_ISCH_TIME
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().warmIschTime().code()]
          }
        }
      }
      if (context.source[WARM_ISCH_TIME_DATE]) {
        extension {
          url = FhirUrls.Extension.Sprec.WARM_ISCH_TIME_DATE
          valueDateTime = context.source[sample().warmIschTimeDate().date()]
        }
      }
      if (context.source[COLD_ISCH_TIME]) {
        extension {
          url = FhirUrls.Extension.Sprec.COLD_ISCH_TIME
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().coldIschTime().code()]
          }
        }
      }
      if (context.source[COLD_ISCH_TIME_DATE]) {
        extension {
          url = FhirUrls.Extension.Sprec.COLD_ISCH_TIME_DATE
          valueDateTime = context.source[sample().coldIschTimeDate().date()]
        }
      }
      if (context.source[STOCK_TYPE]) {
        extension {
          url = FhirUrls.Extension.Sprec.STOCK_TYPE
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().stockType().code()]
          }
        }
      }
      if (context.source[SPREC_FIXATION_TIME]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_FIXATION_TIME
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().sprecFixationTime().code()]
          }
        }
      }
      if (context.source[SPREC_FIXATION_TIME_DATE]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_FIXATION_TIME_DATE
          valueDateTime = context.source[sample().sprecFixationTimeDate().date()]
        }
      }
    }

    //
    // SPREC LIQUID
    //
    if (SampleKind.LIQUID == context.source[SAMPLE_KIND] as SampleKind) {
      if (context.source[SPREC_PRIMARY_SAMPLE_CONTAINER]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_PRIMARY_SAMPLE_CONTAINER
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().sprecPrimarySampleContainer().code()]
          }
        }
      }
      if (context.source[SPREC_PRE_CENTRIFUGATION_DELAY]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_PRE_CENTRIFUGATION_DELAY
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().sprecPreCentrifugationDelay().code()]
          }
        }
      }
      if (context.source[SPREC_PRE_CENTRIFUGATION_DELAY_DATE]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_PRE_CENTRIFUGATION_DELAY_DATE
          valueDateTime = context.source[sample().sprecPreCentrifugationDelayDate().date()]
        }
      }
      if (context.source[SPREC_POST_CENTRIFUGATION_DELAY]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_POST_CENTRIFUGATION_DELAY
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().sprecPostCentrifugationDelay().code()]
          }
        }
      }
      if (context.source[SPREC_POST_CENTRIFUGATION_DELAY_DATE]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_POST_CENTRIFUGATION_DELAY_DATE
          valueDateTime = context.source[sample().sprecPostCentrifugationDelayDate().date()]
        }
      }
      if (context.source[STOCK_PROCESSING]) {
        extension {
          url = FhirUrls.Extension.Sprec.STOCK_PROCESSING
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().stockProcessing().code()] as String
          }
        }
      }
      if (context.source[STOCK_PROCESSING_DATE]) {
        extension {
          url = FhirUrls.Extension.Sprec.STOCK_PROCESSING_DATE
          valueDateTime = context.source[sample().stockProcessingDate().date()]
        }
      }
      if (context.source[SECOND_PROCESSING]) {
        extension {
          url = FhirUrls.Extension.Sprec.SECOND_PROCESSING
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().secondProcessing().code()] as String
          }
        }
      }
      if (context.source[SECOND_PROCESSING_DATE]) {
        extension {
          url = FhirUrls.Extension.Sprec.SECOND_PROCESSING_DATE
          valueDateTime = context.source[sample().secondProcessingDate().date()]
        }
      }
    }
  }
}
