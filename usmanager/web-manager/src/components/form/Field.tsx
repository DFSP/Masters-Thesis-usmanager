import React from "react";
import {mapLabelToIcon} from "../../utils/image";
import {camelCaseToSentenceCase} from "../../utils/text";
import M from "materialize-css";
import {FormContext, IErrors, IFormContext, IValues} from "./Form";
import {TextBox} from "./TextBox";
import {MultilineTextBox} from "./MultilineTextBox";
import {Datepicker} from "./Datepicker";
import {Timepicker} from "./Timepicker";
import {NumberBox} from "./NumberBox";
import {Dropdown} from "./Dropdown";
import {CheckboxList} from "./CheckboxList";
import checkboxListStyles from "./CheckboxList.module.css";

export interface IValidation {
  rule: (values: IValues, id: keyof IValues, args: any) => string;
  args?: any;
}

export interface FieldProps<T = string> {
  id: string;
  type?: "text" | "number" | "date" | "datepicker" | "timepicker" | "multilinetext" | "dropdown" | "list";
  label?: string;
  value?: any;
  valueToString?: (v: T) => string
  dropdown?: { defaultValue: string, values: T[], optionToString?: (v: T) => string, selectCallback?: (value: any) => void };
  number?: { min: number, max: number };
  validation?: IValidation;
  icon?: boolean;
  disabled?: boolean;
}

export const getTypeFromValue = (value: any): 'text' | 'number' =>
  value === undefined
  || value === ''
  || typeof value === 'boolean'
  || typeof value === 'string' && !value.trim().length
  || isNaN(value)
    ? 'text' : 'number';

export default class Field<T> extends React.Component<FieldProps<T>> {

  private updateField = () =>
    M.updateTextFields();

  public componentDidMount(): void {
    this.updateField();
  }

  public componentDidUpdate(prevProps: Readonly<FieldProps<T>>, prevState: Readonly<{}>, snapshot?: any): void {
    this.updateField();
  }

  private onChange = (id: string, formContext: IFormContext, selected?: boolean) => (e: React.FormEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>): void => {
    const target = e.target as HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement;
    let value = target.value;
    if (!isNaN(+value)) {
      value = `"${value}"`;
    }
    try {
      value = JSON.parse(value);
    } catch (_) { }
    if (selected) {
      this.props.dropdown?.selectCallback?.(value);
    }
    formContext.setValue(id, value);
  };

  private onSelect = (value: string, id: string, formContext: IFormContext) => {
    formContext.setValue(id, value);
  };

  private onBlur = (id: string, formContext: IFormContext) => (): void =>
    formContext.validate(id);

  private onCheck = (listId: keyof IValues, itemId: string, checked: boolean, formContext: IFormContext) => {
    if (checked) {
      formContext.addValue(listId, itemId);
    }
    else {
      formContext.removeValue(listId, itemId);
    }
  };

  private getDateStringFromTimestamp = (value: number) => {
    const date = new Date(value * 1000);
    return `${date.toLocaleDateString("pt")} ${date.toLocaleTimeString("pt") }`
  };

  public render() {
    const {id, type, label, dropdown, number, icon, disabled, valueToString} = this.props;
    const getError = (errors: IErrors): string => (errors ? errors[id] : "");
    const getEditorClassname = (errors: IErrors, disabled: boolean, value: string): string => {
      const hasErrors = getError(errors);
      if (hasErrors) {
        return "invalidate-field";
      }
      else if (!hasErrors && !disabled && (getTypeFromValue(value) !== 'text' || value)) {
        return "validate-field";
      }
      else {
        return "no-validation-field";
      }
    };
    return (
      <FormContext.Consumer>
        {(formContext: IFormContext | null) => (
          formContext && (
            <div key={id}
                 className={type !== 'list' ? `input-field col s12` : `input-field col s12 ${checkboxListStyles.listWrapper}`}
                 style={icon !== undefined && !icon ? {marginLeft: '10px'} : undefined}>
              {label && type !== "list" && (
                <>
                  {(icon === undefined || icon) &&
                   <i className="material-icons prefix">{mapLabelToIcon(label)}</i>}
                  <label className="active" htmlFor={id}>
                    {camelCaseToSentenceCase(label)}
                  </label>
                </>
              )}
              {(!type || type.toLowerCase() === "text") && (
                <TextBox<T> className={getEditorClassname(formContext.errors, !formContext.isEditing, formContext.values[id])}
                            id={id}
                            name={id}
                            value={formContext.values[id]}
                            disabled={disabled || !formContext.isEditing}
                            onChange={this.onChange(id, formContext)}
                            valueToString={valueToString}
                            onBlur={this.onBlur(id, formContext)}/>
              )}
              {type && type.toLowerCase() === "number" && (
                <NumberBox className={getEditorClassname(formContext.errors, !formContext.isEditing, formContext.values[id])}
                           id={id}
                           name={id}
                           value={formContext.values[id]}
                           min={number && number.min}
                           max={number && number.max}
                           disabled={disabled || !formContext.isEditing}
                           onChange={this.onChange(id, formContext)}
                           onBlur={this.onBlur(id, formContext)}/>
              )}
              {type && type.toLowerCase() === "date" && (
                <TextBox className={getEditorClassname(formContext.errors, !formContext.isEditing, formContext.values[id])}
                         id={id}
                         name={id}
                         value={this.getDateStringFromTimestamp(formContext.values[id])}
                         disabled={disabled || !formContext.isEditing}
                         onChange={this.onChange(id, formContext)}
                         onBlur={this.onBlur(id, formContext)}/>
              )}
              {(type && type.toLowerCase() === "datepicker") && (
                <Datepicker className={getEditorClassname(formContext.errors, !formContext.isEditing, formContext.values[id])}
                            id={id}
                            name={id}
                            value={formContext.values[id]}
                            disabled={disabled || !formContext.isEditing}
                            onSelect={date => this.onSelect(date, id, formContext)}/>
              )}
              {(type && type.toLowerCase() === "timepicker") && (
                <Timepicker className={getEditorClassname(formContext.errors, !formContext.isEditing, formContext.values[id])}
                            id={id}
                            name={id}
                            value={formContext.values[id]}
                            disabled={disabled || !formContext?.isEditing}
                            onSelect={value => this.onSelect(value, id, formContext)}/>
              )}
              {type && type.toLowerCase() === "multilinetext" && (
                <MultilineTextBox className={getEditorClassname(formContext.errors, !formContext.isEditing, formContext.values[id])}
                                  id={id}
                                  name={id}
                                  value={formContext.values[id]}
                                  disabled={disabled || !formContext.isEditing}
                                  onChange={this.onChange(id, formContext)}
                                  onBlur={this.onBlur(id, formContext)}/>
              )}
              {type && type.toLowerCase() === "dropdown" && dropdown && (
                <Dropdown className={getEditorClassname(formContext.errors, !formContext.isEditing, formContext.values[id])}
                          id={id}
                          name={id}
                          value={formContext.values[id]}
                          disabled={disabled || !formContext.isEditing}
                          onChange={this.onChange(id, formContext, true)}
                          onBlur={this.onBlur(id, formContext)}
                          dropdown={dropdown}/>
              )}
              {(type && type.toLowerCase() === "list") && (
                <CheckboxList id={id}
                              name={id}
                              values={this.props.value}
                              disabled={disabled || !formContext?.isEditing}
                              onCheck={(listId, itemId, checked) => this.onCheck(listId, itemId, checked, formContext)}/>
              )}
              {getError(formContext.errors) && (
                <span className={"helper-text red-text darken-3"}>
                    {getError(formContext.errors)}
                  </span>
              )}
            </div>
          )
        )}
      </FormContext.Consumer>
    )
  }

}