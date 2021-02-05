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

import BaseComponent from "../BaseComponent";
import React, {createRef} from "react";
import M, {ModalOptions} from "materialize-css";
import {IValues} from "../form/Form";
import ScrollBar from "react-perfect-scrollbar";
import ReactTooltip from "react-tooltip";

interface DialogProps {
    id: string;
    title?: string;
    position?: string;
    confirmCallback?: (input: any) => void;
    fullscreen?: boolean;
    scrollbar?: React.RefObject<ScrollBar>;
    locked?: boolean;
    footer?: false;
    onClose?: () => void;
    titleButtons?: JSX.Element;
}

type Props = DialogProps;

interface State {
    fullscreen: boolean;
    scrollMaxHeight: number;
}

export default class Dialog extends BaseComponent<Props, State> {

    private modal = createRef<HTMLDivElement>();
    private scrollbar = this.props.scrollbar || createRef<ScrollBar>();

    constructor(props: Props) {
        super(props);
        this.state = {
            fullscreen: props.fullscreen || false,
            scrollMaxHeight: this.calcDialogScrollMaxHeight(props.fullscreen || false)
        }
    }

    public componentDidMount(): void {
        this.initModal(this.props.position, this.state.fullscreen);
        window.addEventListener('resize', this.handleResize);
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        let modal = M.Modal.getInstance(this.modal.current as Element);
        if (prevState.fullscreen !== this.state.fullscreen
            || prevState.scrollMaxHeight !== this.state.scrollMaxHeight) {
            modal.options.inDuration = 0;
            modal.options.outDuration = 0;
            modal.options.onOpenEnd = this.onOpenModal;
            modal.options.onCloseStart = this.onCloseModal;
            modal.options.preventScrolling = true;
        }
        this.scrollbar?.current?.updateScroll();
    }

    componentWillUnmount() {
        window.removeEventListener('resize', this.handleResize);
    }

    public render() {
        const {id, title, children, locked, footer, confirmCallback, titleButtons} = this.props;
        const {fullscreen} = this.state;
        return (
            <div id={id} className={`modal dialog ${fullscreen ? 'modal-fullscreen' : ''}`} ref={this.modal}>
                <ReactTooltip id='tooltip' effect='solid' type='light'/>
                <ReactTooltip id='dark-tooltip' effect='solid' type='dark'/>
                <div className="modal-content">
                    {title && (
                        <>
                            <div className='modal-title'>
                                {title}
                                {!fullscreen && (
                                    <button className='btn-floating btn-flat right'
                                            data-for='dark-tooltip' data-tip="Ecrã inteiro" data-place={'top'}
                                            onClick={this.toggleFullScreen}
                                            type='button'>
                                        <i className="material-icons">fullscreen</i>
                                    </button>
                                )}
                                {fullscreen && (
                                    <>
                                        <button className='modal-close btn-floating btn-flat right'
                                                type='button'>
                                            <i className="material-icons">close</i>
                                        </button>
                                        {!locked &&
                                        <button className='btn-floating btn-flat right'
                                                onClick={this.toggleFullScreen}
                                                type='button'>
                                            <i className="material-icons">fullscreen_exit</i>
                                        </button>}
                                    </>
                                )}
                                {titleButtons}
                            </div>
                        </>
                    )}
                    <ScrollBar ref={this.scrollbar}
                               component={'div'}
                               style={this.state.scrollMaxHeight ? {maxHeight: Math.floor(this.state.scrollMaxHeight)} : undefined}>
                        {children}
                    </ScrollBar>
                    {(footer === undefined || footer) &&
                    <div
                        className={`modal-footer dialog-footer`}>
                        <div>
                            <button
                                className={`modal-close btn-flat red-text inline-button`}
                                type="button">
                                Cancelar
                            </button>
                            {confirmCallback &&
                            <button className={`btn-flat green-text inline-button`}
                                    type="button"
                                    onClick={this.confirmCallback}>
                                Confirmar
                            </button>}
                        </div>
                    </div>}
                </div>
            </div>
        );
    }

    private initModal = (position?: string, fullScreen?: boolean) => {
        let options: Partial<ModalOptions> = {
            onOpenEnd: this.onOpenModal,
            onCloseStart: this.onCloseModal,
            preventScrolling: true
        };
        if (position) {
            options.startingTop = position;
            options.endingTop = position;
        }
        if (fullScreen) {
            options.startingTop = '0%';
            options.endingTop = '0%';
        }
        M.Modal.init(this.modal.current as Element, options);
    }

    private calcDialogScrollMaxHeight = (fullscreen: boolean) =>
        fullscreen
            //116px because of 58px title and 58px footer
            ? document.body.clientHeight - 116
            : document.body.clientHeight * 0.7 - 116

    private handleResize = () => {
        this.setState({
            scrollMaxHeight: this.calcDialogScrollMaxHeight(this.state.fullscreen)
        })
        this.scrollbar?.current?.updateScroll();
    }

    private onOpenModal = (): void => {
        M.updateTextFields();
        this.scrollbar.current?.updateScroll();
    };

    private onCloseModal = (): void => {
        let modal = M.Modal.getInstance(this.modal.current as Element);
        modal.options.inDuration = 250;
        modal.options.outDuration = 250;
        this.props.onClose?.();
    }

    private confirmCallback = (values: IValues): void => {
        let modal = M.Modal.getInstance(this.modal.current as Element);
        modal.close();
        this.props.confirmCallback?.(values);
    };

    private toggleFullScreen = () => {
        this.setState({fullscreen: !this.state.fullscreen}, () => this.handleResize());
    }

}