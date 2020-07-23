/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import React, {createRef} from "react";
import M from "materialize-css";

interface Props<T> {
  className?: string;
  id: string;
  name: string;
  value: any;
  disabled?: boolean;
  dropdown: { defaultValue?: string | number, values: T[], optionToString?: (v: T) => string };
  onChange: (e: React.FormEvent<HTMLSelectElement>) => void;
  onBlur?: (e: React.FormEvent<HTMLSelectElement>) => void;
}

export class Dropdown<T> extends React.Component<Props<T>, {}> {

  private dropdown = createRef<HTMLSelectElement>();

  public componentDidMount(): void {
    this.initDropdown();
  }

  public componentDidUpdate(prevProps: Readonly<Props<T>>, prevState: Readonly<{}>, snapshot?: any): void {
    this.initDropdown();
    M.updateTextFields();
  }

  public render() {
    const {className, id, name, value, disabled, onChange, onBlur, dropdown} = this.props;
    let valueString = value === undefined ? "" : value;
    valueString = typeof valueString === 'object' ? JSON.stringify(valueString) : valueString.toString();
    return (
      <select
        className={className}
        id={id}
        name={name}
        value={valueString}
        disabled={disabled}
        onChange={onChange}
        onBlur={onBlur}
        ref={this.dropdown}>
        {<>
          {dropdown.defaultValue && (
            <option key={dropdown.defaultValue} value="" disabled hidden>
              {dropdown.defaultValue}
            </option>
          )}
          {dropdown.values.map((option, index) => {
              return (
                <option key={index}
                        value={typeof option === 'string' || typeof option === 'boolean' ? option.toString() : JSON.stringify(option)}>
                  {typeof option === 'string' || typeof option === 'boolean'
                    ? option.toString()
                    : dropdown.optionToString?.(option)}
                </option>
              );
            }
          )}
        </>}
      </select>
    )
  }

  private initDropdown = (): void => {
    M.FormSelect.init(this.dropdown.current as Element);
  };

}
