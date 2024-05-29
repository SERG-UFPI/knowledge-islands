import * as React from 'react';
import { Card, Row, Col, Accordion, Container, Button, Modal } from "react-bootstrap";
import { useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import { RichTreeView } from '@mui/x-tree-view/RichTreeView';
import FolderIcon from '@mui/icons-material/Folder';
import InsertDriveFileOutlinedIcon from '@mui/icons-material/InsertDriveFileOutlined';
import axios from "axios";
import ContributorPaginatedTable from "../components/ContributorPaginatedTable";
import ContributorVersionPaginatedTable from "../components/ContributorVersionPaginatedTable";
import FileVersionPaginatedTable from '../components/FileVersionPaginatedTable';
import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Chip from '@mui/material/Chip';
import GroupsIcon from '@mui/icons-material/Groups';
import { unstable_useTreeItem2 as useTreeItem2 } from '@mui/x-tree-view/useTreeItem2';
import {
    TreeItem2Content,
    TreeItem2IconContainer,
    TreeItem2GroupTransition,
    TreeItem2Label,
    TreeItem2Root,
} from '@mui/x-tree-view/TreeItem2';
import { TreeItem2Icon } from '@mui/x-tree-view/TreeItem2Icon';
import { TreeItem2Provider } from '@mui/x-tree-view/TreeItem2Provider';
import VisibilityIcon from '@mui/icons-material/Visibility';
import FilePaginatedTable from '../components/FilePaginatedTable';


const GitRepositoryVersion = () => {
    const location = useLocation();
    const [showModal, setShowModal] = useState(false);
    const [selectedItems, setSelectedItems] = useState([]);
    const [gitRepositoryVersion, setGitRepositoryVersion] = useState(null);
    const [itemsRepo, setItemsRepo] = useState([]);
    const [truckFactorSelected, setTruckFactorSelected] = useState(null);
    let id = location?.state?.id;
    useEffect(() => {
        axios.get(`http://localhost:8080/api/git-repository-version/${id}`, { withCredentials: true })
            .then(response => {
                console.log(response.data);
                setGitRepositoryVersion(response.data);
                const updatedItemsRepo = [...itemsRepo, response.data.rootFolder];
                setSelectedItems(response.data.rootFolder.id);
                setItemsRepo(updatedItemsRepo);
            });
    }, []);
    const handleSelectedItemsChange = (event, ids) => {
        setSelectedItems(ids);
    };
    const CustomTreeItemContent = styled(TreeItem2Content)(({ theme }) => ({
        padding: theme.spacing(0.5, 1),
    }));
    function findById(id, nodes) {
        for (const node of nodes) {
            if (node.id === id) {
                return node;
            }
            if (node.children) {
                const result = findById(id, node.children);
                if (result) {
                    return result;
                }
            }
        }
        return null;
    }
    const handleClose = () => setShowModal(false);
    const handleShow = () => {
        if (selectedItems === itemsRepo[0].id) {
            setTruckFactorSelected(itemsRepo[0]);
        } else {
            setTruckFactorSelected(findById(selectedItems, itemsRepo[0].children));
        }
        setShowModal(true);
    };
    const CustomTreeItem = React.forwardRef(function CustomTreeItem(props, ref) {
        const { id, itemId, label, disabled, children, ...other } = props;

        const {
            getRootProps,
            getContentProps,
            getIconContainerProps,
            getCheckboxProps,
            getLabelProps,
            getGroupTransitionProps,
            status,
            publicAPI,
        } = useTreeItem2({ id, itemId, children, label, disabled, rootRef: ref });
        const item = publicAPI.getItem(itemId);
        return (
            <TreeItem2Provider itemId={itemId}>
                <TreeItem2Root {...getRootProps(other)}>
                    <CustomTreeItemContent {...getContentProps()}>
                        <TreeItem2IconContainer {...getIconContainerProps()}>
                            <TreeItem2Icon status={status} />
                        </TreeItem2IconContainer>

                        <Box sx={{ flexGrow: 1, display: 'flex', gap: 1 }}>
                            <TreeItem2Label {...getLabelProps()} />
                            <Chip color="primary" icon={<GroupsIcon />} label={<span style={{ fontWeight: 'bold' }}>{"Truck Factor=" + item.truckFactor?.truckFactor}</span>} />
                        </Box>

                    </CustomTreeItemContent>
                    {children && <TreeItem2GroupTransition {...getGroupTransitionProps()} />}
                </TreeItem2Root>
            </TreeItem2Provider>
        );
    });
    return (
        <>
            <Modal size="xl" show={showModal} onHide={handleClose}>
                <Modal.Header closeButton>
                    <Modal.Title>'{truckFactorSelected?.label}' details</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p><b>Truck Factor: </b>{truckFactorSelected?.truckFactor.truckFactor}</p>
                    <ContributorVersionPaginatedTable contributorsVersions={truckFactorSelected?.truckFactor?.contributors} />
                    <br />
                    <FileVersionPaginatedTable filesVersions={truckFactorSelected?.files} />
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={handleClose}>
                        Close
                    </Button>
                </Modal.Footer>
            </Modal>
            <br />
            <div className={"text-center"}>
                <h3>{gitRepositoryVersion?.gitRepository.fullName}</h3>
            </div>
            <Card>
                <br />
                <Container>
                    <Row>
                        <Col><b>Version date:</b> {gitRepositoryVersion?.dateVersion}</Col>
                        <Col><b>Version id:</b> {gitRepositoryVersion?.versionId}</Col>
                    </Row>
                    <br />
                    <Row>
                        <Col><b>Number of analyzed devs:</b> {gitRepositoryVersion?.numberAnalysedDevs}</Col>
                        <Col><b>Number of analyzed files:</b> {gitRepositoryVersion?.numberAnalysedFiles}</Col>
                        <Col><b>Number of analyzed commits:</b> {gitRepositoryVersion?.numberAnalysedCommits}</Col>
                    </Row>
                    <br />
                    <Row>
                        <Col><b>Repository Truck Factor:</b> {gitRepositoryVersion?.rootFolder?.truckFactor?.truckFactor}</Col>
                    </Row>
                </Container>
                <br />
            </Card>
            <br />
            <Card>
                <Card.Body>
                    <Card.Title>Folder Structure</Card.Title>
                    <RichTreeView
                        defaultExpandedItems={["1"]}
                        selectedItems={selectedItems}
                        onSelectedItemsChange={handleSelectedItemsChange}
                        slots={{
                            item: CustomTreeItem,
                            expandIcon: FolderIcon,
                            collapseIcon: FolderIcon,
                            endIcon: InsertDriveFileOutlinedIcon,
                        }}
                        items={itemsRepo} />
                </Card.Body>
            </Card>
            <br />
            <Button variant="primary" type="button" disabled={selectedItems.length === 0}
                onClick={handleShow}>
                <VisibilityIcon /> See Details
            </Button>
            <br /><br />
            <Accordion>
                <Accordion.Item eventKey="0">
                    <Accordion.Header>
                        Contributors
                    </Accordion.Header>
                    <Accordion.Body>
                        <ContributorPaginatedTable gitRepositoryVersion={gitRepositoryVersion} />
                    </Accordion.Body>
                </Accordion.Item>
                <Accordion.Item eventKey="1">
                    <Accordion.Header>
                        Files
                    </Accordion.Header>
                    <Accordion.Body>
                        <FilePaginatedTable files={gitRepositoryVersion?.files} />
                    </Accordion.Body>
                </Accordion.Item>
            </Accordion>
        </>
    );
};

export default GitRepositoryVersion;