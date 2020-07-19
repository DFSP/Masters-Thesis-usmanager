import BaseComponent from "../../../components/BaseComponent";
import ListItem from "../../../components/list/ListItem";
import styles from "../../../components/list/ListItem.module.css";
import List from "../../../components/list/List";
import React from "react";
import {IContainer, IContainerPort} from "./Container";

interface ContainerPortsListProps {
  isLoadingContainer: boolean;
  loadContainerError?: string | null;
  container?: IContainer | null;
}

type Props = ContainerPortsListProps;

export default class ContainerPortsList extends BaseComponent<Props, {}> {

  public render() {
    const PortsList = List<IContainerPort>();
    return (
      <PortsList isLoading={this.props.isLoadingContainer}
                 error={this.props.loadContainerError}
                 emptyMessage={`No ports associated`}
                 list={this.ports()}
                 show={this.port}/>
    );
  }

  private ports = () =>
    this.props.container?.ports || [];

  private port = (port: IContainerPort, index: number): JSX.Element =>
    <ListItem key={index} separate={index !== this.ports().length - 1}>
      <div className={`${styles.listItemContent}`}>
        <span>{port.privatePort}:{port.publicPort} {port.ip}/{port.type}</span>
      </div>
    </ListItem>;

}
