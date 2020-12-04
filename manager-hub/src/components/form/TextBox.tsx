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

import React from "react";
import M from "materialize-css";

interface TextBoxProps<T> {
    className: string;
    id: string;
    name: string;
    value?: any;
    disabled?: boolean;
    valueToString?: (v: T) => string
    onChange: (e: React.FormEvent<HTMLInputElement>) => void;
    onBlur: (e: React.FormEvent<HTMLInputElement>) => void;
    hidden?: boolean;
}

interface State {
    show: boolean;
}

export class TextBox<T> extends React.Component<TextBoxProps<T>, State> {

    state = {
        show: this.props.hidden === undefined || !this.props.hidden,
    };

    public componentDidUpdate(prevProps: Readonly<TextBoxProps<T>>, prevState: Readonly<any>, snapshot?: any): void {
        M.updateTextFields();
    }

    render(): any {
        const {className, id, name, value, disabled, hidden, valueToString, onChange, onBlur} = this.props;
        const {show} = this.state;
        return (
            <>
                <input
                    className={className}
                    id={id}
                    name={name}
                    type={show ? "text" : "password"}
                    value={typeof value === 'object' && valueToString ? valueToString(value) : (value !== undefined ? value : '')}
                    disabled={disabled}
                    autoComplete="off"
                    onChange={onChange}
                    onBlur={onBlur}
                    formNoValidate={true}/>
                {hidden !== undefined && <i className="material-icons suffix" onClick={this.handleToggleVisibility}>
                    {show ? "visibility" : "visibility_off"}
                </i>}
            </>
        )
    }

    private handleToggleVisibility = () =>
        this.setState({show: !this.state.show});

}
