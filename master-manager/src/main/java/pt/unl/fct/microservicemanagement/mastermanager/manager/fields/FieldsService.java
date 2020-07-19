package pt.unl.fct.microservicemanagement.mastermanager.manager.fields;

import pt.unl.fct.microservicemanagement.mastermanager.exceptions.EntityNotFoundException;
import pt.unl.fct.microservicemanagement.mastermanager.util.ObjectUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FieldsService {

  private final FieldRepository fields;

  public FieldsService(FieldRepository fields) {
    this.fields = fields;
  }

  public List<FieldEntity> getFields() {
    return fields.findAll();
  }

  public FieldEntity getField(Long id) {
    return fields.findById(id).orElseThrow(() ->
        new EntityNotFoundException(FieldEntity.class, "id", id.toString()));
  }

  public FieldEntity getField(String name) {
    return fields.findByNameIgnoreCase(name).orElseThrow(() ->
        new EntityNotFoundException(FieldEntity.class, "name", name));
  }

  public FieldEntity addField(FieldEntity field) {
    assertFieldDoesntExist(field);
    log.debug("Saving field {}", ToStringBuilder.reflectionToString(field));
    return fields.save(field);
  }

  public FieldEntity updateField(String fieldName, FieldEntity newField) {
    var field = getField(fieldName);
    log.debug("Updating field {} with {}",
        ToStringBuilder.reflectionToString(field), ToStringBuilder.reflectionToString(newField));
    log.debug("Field before copying properties: {}",
        ToStringBuilder.reflectionToString(field));
    ObjectUtils.copyValidProperties(newField, field);
    log.debug("Field after copying properties: {}",
        ToStringBuilder.reflectionToString(field));
    field = fields.save(field);
    return field;
  }

  public void deleteField(String fieldName) {
    var field = getField(fieldName);
    fields.delete(field);
  }

  private void assertFieldDoesntExist(FieldEntity field) {
    var fieldName = field.getName();
    if (fields.hasField(fieldName)) {
      throw new DataIntegrityViolationException("Field '" + fieldName + "' already exists");
    }
  }

}
