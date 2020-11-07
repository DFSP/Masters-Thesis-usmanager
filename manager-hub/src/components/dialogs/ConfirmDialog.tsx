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
import {bindActionCreators} from "redux";
import {hideConfirmationDialog} from "../../actions";
import {connect} from "react-redux";
import styles from './ConfirmDialog.module.css';


interface DispatchToProps {
    hideConfirmationDialog: (hidden: boolean) => void;
}

interface ConfirmDialogProps {
    id: string;
    message: string;
    cancel?: boolean;
    cancelCallback?: () => void;
    confirm?: boolean;
    confirmCallback?: () => void;
}

type Props = DispatchToProps & ConfirmDialogProps;

class ConfirmDialog extends React.Component<Props, {}> {

    private modal = createRef<HTMLDivElement>();
    private checkbox = createRef<HTMLInputElement>();

    public componentDidMount(): void {
        this.initModal();
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<{}>, snapshot?: any): void {
        this.initModal();
    }

    private initModal = () => {
        M.Modal.init(this.modal.current as Element, {
            /*startingTop: '38.5%',
            endingTop: '38.5%',*/
            preventScrolling: false
        });
    }

    private handleCancel = () => {
        this.props.cancelCallback?.();
        this.props.hideConfirmationDialog(this.checkbox?.current?.checked || false);
    }

    private handleConfirm = () => {
        this.props.confirmCallback?.();
        this.props.hideConfirmationDialog(this.checkbox?.current?.checked || false);
    }

    public render() {
        const {message} = this.props;
        return (
            <div id={this.props.id} className='modal dialog' ref={this.modal}>
                <div className="modal-content">
                    <div className="modal-message">{`Are you sure you want ${message.startsWith('to ') ? 'to ' : ''}`}
                        <div
                            className="dialog-message">{message.startsWith('to ') ? message.substr(3, message.length) : message}</div>
                        ?
                    </div>
                </div>
                <div className={`modal-footer dialog-footer`}>
                    <div className={`${styles.hideSection}`}>
                        <label>
                            <input ref={this.checkbox}
                                   id={'disable-confirmation-dialog'}
                                   type="checkbox"/>
                            <span className={`${styles.hideMessage}`}>Don't show me this again</span>
                        </label>
                    </div>
                    <button className={`modal-close btn-flat white-text ${styles.actionButton}`}
                            onClick={this.handleCancel}>
                        No
                    </button>
                    <button className={`modal-close btn-flat white-text ${styles.actionButton}`}
                            onClick={this.handleConfirm}>
                        Absolutely
                    </button>
                </div>
            </div>
        );
    }

}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({hideConfirmationDialog}, dispatch);

export default connect(null, mapDispatchToProps)(ConfirmDialog);