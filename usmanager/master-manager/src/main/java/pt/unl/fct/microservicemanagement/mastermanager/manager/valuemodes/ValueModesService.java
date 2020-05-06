package pt.unl.fct.microservicemanagement.mastermanager.manager.valuemodes;

import pt.unl.fct.microservicemanagement.mastermanager.exceptions.EntityNotFoundException;
import pt.unl.fct.microservicemanagement.mastermanager.util.ObjectUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ValueModesService {

  private final ValueModeRepository valueModes;

  public ValueModesService(ValueModeRepository valueModes) {
    this.valueModes = valueModes;
  }

  public Iterable<ValueModeEntity> getValueModes() {
    return valueModes.findAll();
  }

  public ValueModeEntity getValueMode(Long id) {
    return valueModes.findById(id).orElseThrow(() ->
        new EntityNotFoundException(ValueModeEntity.class, "id", id.toString()));
  }

  public ValueModeEntity getValueMode(String valueModeName) {
    return valueModes.findByNameIgnoreCase(valueModeName).orElseThrow(() ->
        new EntityNotFoundException(ValueModeEntity.class, "name", valueModeName));
  }

  public ValueModeEntity addValueMode(ValueModeEntity valueMode) {
    log.debug("Saving valueMode {}", ToStringBuilder.reflectionToString(valueMode));
    return valueModes.save(valueMode);
  }

  public ValueModeEntity updateValueMode(String valueModeName, ValueModeEntity newValueMode) {
    var valueMode = getValueMode(valueModeName);
    log.debug("Updating valueMode {} with {}",
        ToStringBuilder.reflectionToString(valueMode), ToStringBuilder.reflectionToString(newValueMode));
    log.debug("valueMode before copying properties: {}",
        ToStringBuilder.reflectionToString(valueMode));
    ObjectUtils.copyValidProperties(newValueMode, valueMode);
    log.debug("valueMode after copying properties: {}",
        ToStringBuilder.reflectionToString(valueMode));
    valueMode = valueModes.save(valueMode);
    return valueMode;
  }

  public void deleteValueMode(String valueModeName) {
    var valueMode = getValueMode(valueModeName);
    valueModes.delete(valueMode);
  }

}
