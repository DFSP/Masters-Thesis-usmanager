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

interface Props {
    id: string;
    message: string;
    cancel?: boolean;
    cancelCallback?: () => void;
    confirm?: boolean;
    confirmCallback?: () => void;
}

export default class ConfirmDialog extends React.Component<Props, {}> {

    private modal = createRef<HTMLDivElement>();

    public componentDidMount(): void {
        this.initModal();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
        this.initModal();
    }

    private initModal = () =>
        M.Modal.init(this.modal.current as Element, {
            startingTop: '38.5%',
            endingTop: '38.5%',
            preventScrolling: false
        });

    public render() {
        const {message} = this.props;
        return (
            <div id={this.props.id} className='modal dialog' ref={this.modal}>
                <div className="modal-content">
                    <div className="modal-message">{`Are you sure you want ${message.startsWith('to ') ? 'to ' : ''}`}<div
                        className="dialog-message">{message.startsWith('to ') ? message.substr(3, message.length) : message}</div>?
                    </div>
                </div>
                <div className={`modal-footer dialog-footer`}>
                    <button className="modal-close waves-effect waves-red btn-flat white-text"
                            onClick={this.props.cancelCallback}>
                        No
                    </button>
                    <button className="modal-close waves-effect waves-green btn-flat white-text"
                            onClick={this.props.confirmCallback}>
                        Absolutely
                    </button>
                </div>
            </div>
        );
    }

}