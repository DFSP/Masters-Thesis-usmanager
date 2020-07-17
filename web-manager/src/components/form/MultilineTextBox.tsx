import React from "react";

interface Props {
  className: string;
  id: string;
  name: string;
  value: string;
  disabled?: boolean;
  onChange: (e: React.FormEvent<HTMLTextAreaElement>) => void;
  onBlur: (e: React.FormEvent<HTMLTextAreaElement>) => void;
}

export class MultilineTextBox extends React.Component<Props, {}> {

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
    M.updateTextFields();
  }

  render(): any {
    const {className, id, name, value, disabled, onChange, onBlur} = this.props;
    return (
      <textarea
        className={`materialize-textarea ${className}`}
        id={id}
        name={name}
        value={value || ''}
        disabled={disabled}
        onChange={onChange}
        onBlur={onBlur}/>
    )
  }

}
