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
import ScrollBar from "react-perfect-scrollbar";
import {Link} from "react-router-dom";
import CardTitle from "../list/CardTitle";
import {ReduxState} from "../../reducers";
import {connect} from "react-redux";
import {ContextMenu, ContextMenuTrigger, MenuItem} from "react-contextmenu";
import ConfirmDialog from "../dialogs/ConfirmDialog";
import {deleteData} from "../../utils/api";
import {RestOperation} from "../form/Form";
import ActionProgressBar from "../actionloading/ActionProgressBar";
import LinkedContextMenuItem from "../contextmenu/LinkedContextMenuItem";
import DividerContextMenuItem from "../contextmenu/DividerContextMenuItem";

interface CardProps<T> {
    id: string;
    title?: string;
    link?: { to: { pathname: string, state: T } };
    height?: number | string;
    margin?: number | string;
    hoverable?: boolean;
    children?: any[];
    delete?: RestOperation;
    loading?: boolean;
    topContextMenuItems?: JSX.Element[];
    bottomContextMenuItems?: JSX.Element[];
}

interface State {
    loading: boolean;
}

interface StateToProps {
    sidenavVisible: boolean;
    confirmationDialog: boolean,
}

type Props<T> = CardProps<T> & StateToProps;

class GenericCard<T> extends React.Component<Props<T>, State> {

    private CARD_ITEM_HEIGHT = 39;

    private scrollbar: (ScrollBar | null) = null;
    private card = createRef<HTMLDivElement>();
    private cardContent = createRef<HTMLDivElement>();

    constructor(props: Props<T>) {
        super(props);
        this.state = {
            loading: props.loading || false
        }
    }

    public componentDidMount(): void {
        this.scrollbar?.updateScroll();
        this.blockBodyScroll();
    }

    componentDidUpdate(prevProps: Readonly<Props<T>>, prevState: Readonly<{}>, snapshot?: any) {
        if (prevProps.sidenavVisible !== this.props.sidenavVisible) {
            this.scrollbar?.updateScroll();
        }
    }

    public render() {
        const {id, title, link, margin, topContextMenuItems, bottomContextMenuItems, confirmationDialog} = this.props;
        const {loading} = this.state;
        const cardElement =
            <>
                <ActionProgressBar loading={loading} backgroundColor={"#1F1F1F"} progressBarColor={"#e51220"}/>
                {this.cardElement()}
            </>;
        return (
            <>
                <ContextMenuTrigger id={`contextMenu-${id}`}>
                    <div className={`col s6 m4 l3`} style={{margin}}>
                        {link
                            ? <Link to={{
                                pathname: link?.to.pathname,
                                state: link?.to.state
                            }}>
                                {cardElement}
                            </Link>
                            : {cardElement}}
                    </div>
                </ContextMenuTrigger>
                <ContextMenu id={`contextMenu-${id}`}>
                    {topContextMenuItems?.map((menuItem, index) =>
                        <div key={index}>
                            {index > 0 && <MenuItem className="react-contextmenu-item--divider"/>}
                            {menuItem}
                        </div>
                    )}
                    {link &&
                    <>
                        {topContextMenuItems?.length
                            ?
                            <>
                                <DividerContextMenuItem/>
                                <DividerContextMenuItem/>
                                <DividerContextMenuItem/>
                                <DividerContextMenuItem/>
                            </>
                            : undefined}
                        <LinkedContextMenuItem option={'Details'}
                                               pathname={link?.to.pathname}
                                               state={{data: link?.to.state}}/>
                    </>}
                    {bottomContextMenuItems?.map((menuItem, index) =>
                        <div key={index}>
                            {(topContextMenuItems?.length || link) && <DividerContextMenuItem/>}
                            {menuItem}
                        </div>
                    )}
                    {this.props.delete !== undefined
                    && <div className={`${loading || !confirmationDialog ? '' : 'modal-trigger'}`} data-target={id}>
                        {(topContextMenuItems?.length || link || bottomContextMenuItems?.length) &&
                        <DividerContextMenuItem/>}
                        <MenuItem
                            className={`${loading ? 'react-contextmenu-item--disabled' : undefined} red-text`}
                            data={this.props.link?.to.state}
                            disabled={loading}
                            onClick={confirmationDialog ? undefined : this.onDelete}>
                            {this.props.delete.textButton || 'Delete'}
                        </MenuItem>
                    </div>}
                </ContextMenu>
                {this.props.delete !== undefined
                && confirmationDialog
                && <ConfirmDialog id={id}
                                  message={`${this.props.delete.confirmMessage || `to ${this.props.delete.textButton?.toLowerCase() || 'delete'} ${title}`}`}
                                  confirmCallback={this.onDelete}/>}
            </>
        )
    }

    private onDelete = () => {
        const {delete: deleteCard} = this.props;
        if (deleteCard) {
            const args = this.props.link?.to.state;
            deleteData(deleteCard.url,
                () => deleteCard.successCallback(args),
                (reply) => deleteCard.failureCallback(reply, args));
            this.setState({loading: true});
        }
    }

    private getChildrenCount = (): number =>
        React.Children.count(this.props.children);

    private getHeight = (): number => {
        let height = this.props.height || this.getChildrenCount() * this.CARD_ITEM_HEIGHT;
        if (typeof height === 'string') {
            height = Number(height.replace(/[^0-9]/g, ''));
        }
        return height;
    };

    private blockBodyScroll = () => {
        const cardContent = this.cardContent.current;
        if (cardContent && cardContent.scrollHeight > this.getHeight()) {
            this.card.current?.addEventListener('wheel', event => event.preventDefault())
        }
    };

    private cardElement = (): JSX.Element => {
        const {title, hoverable, children} = this.props;
        const childrenCount = this.getChildrenCount();
        return (
            <div className={hoverable ? 'hoverable' : undefined}
                 style={childrenCount === 0 ? {borderBottom: '1px black solid'} : undefined}>
                {title && <CardTitle title={title}/>}
                {childrenCount > 0 && (
                    <div className={`card gridCard`}
                         style={{height: this.getHeight()}}
                         ref={this.card}>
                        <ScrollBar ref={(ref) => {
                            this.scrollbar = ref;
                        }}
                                   component="div">
                            <div className='card-content' ref={this.cardContent}>
                                {children}
                            </div>
                        </ScrollBar>
                    </div>)}
            </div>
        )
    };

}

const mapStateToProps = (state: ReduxState): StateToProps => (
    {
        sidenavVisible: state.ui.sidenav.user && state.ui.sidenav.width,
        confirmationDialog: state.ui.confirmationDialog,
    }
);

export default function Card<T>() {
    return connect(mapStateToProps)(GenericCard as new(props: Props<T>) => GenericCard<T>);
}
