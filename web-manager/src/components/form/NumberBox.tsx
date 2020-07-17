import React from "react";

interface NumberBoxProps {
  className: string;
  id: string;
  name: string;
  value?: string;
  disabled?: boolean;
  min?: number;
  max?: number;
  onChange: (e: React.FormEvent<HTMLInputElement>) => void;
  onBlur: (e: React.FormEvent<HTMLInputElement>) => void;
}

export class NumberBox extends React.Component<NumberBoxProps, any> {

  public componentDidUpdate(prevProps: Readonly<NumberBoxProps>, prevState: Readonly<any>, snapshot?: any): void {
    M.updateTextFields();
  }

  render(): any {
    const {className, id, name, min, max, value, disabled, onChange, onBlur} = this.props;
    return (
      <input
        className={className}
        id={id}
        name={name}
        type="number"
        value={value === undefined ? '' : value}
        min={min}
        max={max}
        disabled={disabled}
        autoComplete="off"
        onChange={onChange}
        onBlur={onBlur}
        formNoValidate={true}/>
    )
  }

}
