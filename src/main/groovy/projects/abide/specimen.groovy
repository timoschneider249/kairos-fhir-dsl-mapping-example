package projects.abide
import de.kairos.fhir.centraxx.metamodel.AbstractIdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import org.hl7.fhir.r4.model.Coding

import java.time.LocalDate

import static de.kairos.fhir.centraxx.metamodel.PrecisionDate.DATE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.abstractSample
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * Represented by a CXX SAMPLE
 * Used in Frankfurt to upload sample data which is at least 6 months old. This means documentation should be finished.
 * @author Timo Schneider
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */
specimen {
    if (!isDateSixMonthsAgo(context.source[sample().samplingDate()]?.getAt(DATE) as String, 6)) {
        return // Return nothing if date is not between 6 months and 5 months ago
    }
    id = "Specimen/" + context.source[sample().id()]

    meta {
        profile "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/ProfileSpecimenBioprobe"
    }

    if (context.source[abstractSample().diagnosis()]) {
        extension {
            url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Diagnose"
            valueReference {
                reference = "Condition/" + context.source[sample().diagnosis().id()]
            }
        }
    }

    if (context.source[sample().organisationUnit()]) {
        extension {
            url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/VerwaltendeOrganisation"
            valueReference {
                reference = "Organization/" + context.source[sample().organisationUnit().id()]
            }
        }
    }


    context.source[sample().idContainer()].each { final def idObj ->
        identifier {
            type {
                coding {
                    system = "urn:centraxx"
                    code = idObj[AbstractIdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE] as String
                    display = idObj[AbstractIdContainer.ID_CONTAINER_TYPE][IdContainerType.NAME] as String
                }
            }
            value = idObj[AbstractIdContainer.PSN]
        }
    }

    // Specimen status is customized in CXX. Exact meaning depends on implementation in CXX. Here, it is assumed that the codes of the codesystem
    // are implemented in CXX.
    status = context.source[abstractSample().restAmount().amount()] > 0 ? "available" : "unavailable"

    if (context.source[sample().sampleType()]) {
        type {
            // SPREC is implemented in CXX.
            coding {
                system = "https://doi.org/10.1089/bio.2017.0109/type-of-sample"
                code = context.source[sample().sampleType().sprecCode()]
            }
        }
    }
    subject {
        reference = "Patient/" + context.source[sample().patientContainer().id()]
    }

    receivedTime {
        date = context.source[sample().receiptDate()]?.getAt(DATE)
    }

    if (context.source[sample().parent()]) {
        parent {
            reference = "Specimen/" + context.source[sample().parent().id()]
        }
    }

    collection {
        collectedDateTime = context.source[sample().samplingDate().date()]
        if (context.source[sample().orgSample()]) {
            bodySite {
                //Organs are specified user-defined in CXX. sct coding only applies, when used for coding in CXX
                coding {
                    system = "http://snomed.info/sct"
                    code = context.source[sample().orgSample().code()]
                    display = context.source[sample().orgSample().nameMultilingualEntries()].find { final def entry ->
                        "de" == entry[MultilingualEntry.LANG]
                    }[MultilingualEntry.VALUE]
                }
            }
        }
    }

    if (context.source[sample().sampleLocation()]) {
        processing {
            procedure {
                coding = [new Coding("https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/CodeSystem/Probenlagerung",
                        "LAGERUNG",
                        "Lagerung einer Probe")]
            }
        }
        timeDateTime = context.source[sample().repositionDate().date()]
    }

    if (context.source[sample().receptable()]) {
        container {
            type {
                coding {
                    system = "https://doi.org/10.1089/bio.2017.0109/long-term-storage"
                    code = context.source[sample().receptable().sprecCode()]
                }
            }
            capacity {
                value = context.source[sample().receptable().size()]
                unit = context.source[sample().receptable().volume()]
            }
            specimenQuantity {
                value = context.source[sample().restAmount().amount()]
                unit = context.source[sample().restAmount().unit()]
            }
            additiveReference {
                if (context.source[sample().sprecPrimarySampleContainer()]){
                    additiveCodeableConcept {
                        coding {
                            system = "https://doi.org/10.1089/bio.2017.0109/type-of-primary-container"
                            code = context.source[sample().sprecPrimarySampleContainer().sprecCode()]
                        }
                    }
                }
                if (context.source[sample().stockType()]){
                    additiveCodeableConcept {
                        coding {
                            system = "https://doi.org/10.1089/bio.2017.0109/type-of-primary-container"
                            code = context.source[sample().stockType().sprecCode()]
                        }
                    }
                }
            }
        }
    }

    note {
        text = context.source[sample().note()] as String
    }
}

static def isDateSixMonthsAgo(final String dateString, int monthOffset) {
    System.out.println(dateString)
    if (dateString == null) {
        return false
    }
    final LocalDate date = LocalDate.parse(dateString.substring(0, 10))
    // Aktuelles Datum
    LocalDate dateNow = LocalDate.now()

    // Berechnen des Datums von vor 6 Monaten
    LocalDate sixMonthsAgo = dateNow.minusMonths(monthOffset)

    // Berechnen des ersten Tages des Monats vor 6 Monaten
    LocalDate firstDayOfMonthSixMonthsAgo = sixMonthsAgo.withDayOfMonth(1)

    // Berechnen des letzten Tages des Monats vor 6 Monaten
    LocalDate lastDayOfMonthSixMonthsAgo = sixMonthsAgo.withDayOfMonth(1).plusMonths(1).minusDays(1);

    return date.isAfter(firstDayOfMonthSixMonthsAgo) && date.isBefore(lastDayOfMonthSixMonthsAgo)
}
