import React, { useState, useEffect, Fragment } from "react";
import { Table, Button, Card } from "react-bootstrap";
import { FaArrowUp, FaArrowDown } from "react-icons/fa";
import FileCopyIcon from '@mui/icons-material/FileCopy';

const ContributorVersionPaginatedTable = ({ contributorsVersions }) => {
    const [sort_Files, set_Sort_Files] = useState("files");
    const [sort_Order, set_Sort_Order] = useState("desc");

    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 5;
    const totalItems = contributorsVersions.length || 0;
    const totalPages = Math.ceil(totalItems / itemsPerPage);

    const [currentItems, setCurrentItems] = useState([]);

    useEffect(() => {
        const initializedContributors = contributorsVersions.map(contributorVersion => ({
            ...contributorVersion,
            expanded: false,
            innerPage: 1,  // Add innerPage state for pagination of filesAuthorPath
        }));

        const startIndex = (currentPage - 1) * itemsPerPage;
        const endIndex = startIndex + itemsPerPage;
        const fileSlice = initializedContributors.slice(startIndex, endIndex);
        setCurrentItems(fileSlice);
    }, [contributorsVersions, currentPage]);

    const handlePreviousPage = () => {
        setCurrentPage((prevPage) => Math.max(prevPage - 1, 1));
    };

    const handleNextPage = () => {
        setCurrentPage((prevPage) => Math.min(prevPage + 1, totalPages));
    };

    const sortFunction = (f) => {
        if (f === "files") {
            if (sort_Files === "files") {
                set_Sort_Order(sort_Order === "asc" ? "desc" : "asc");
            } else {
                set_Sort_Files("files");
                set_Sort_Order("asc");
            }
        }
        const sorted = [...currentItems].sort((a, b) => {
            const multi = sort_Order === "asc" ? 1 : -1;
            return multi * (a["numberFilesAuthor"] - b["numberFilesAuthor"]);
        });
        setCurrentItems(sorted);
    };

    const handleExpandClick = (id) => {
        const updatedItems = currentItems.map((item, index) => {
            if (index === id) {
                return { ...item, expanded: !item.expanded, innerPage: 1 };  // Reset innerPage to 1 on expand/collapse
            }
            return item;
        });
        setCurrentItems(updatedItems);
    };

    const handleInnerPreviousPage = (id) => {
        const updatedItems = currentItems.map((item, index) => {
            if (index === id) {
                return { ...item, innerPage: Math.max(item.innerPage - 1, 1) };
            }
            return item;
        });
        setCurrentItems(updatedItems);
    };

    const handleInnerNextPage = (id) => {
        const updatedItems = currentItems.map((item, index) => {
            if (index === id) {
                const innerTotalPages = Math.ceil(item.filesAuthorPath.length / itemsPerPage);
                return { ...item, innerPage: Math.min(item.innerPage + 1, innerTotalPages) };
            }
            return item;
        });
        setCurrentItems(updatedItems);
    };

    return (
        <>
            <Card>
                <Card.Body>
                    <Card.Title>Truck Factor Developers</Card.Title>
                    <Table bordered hover id="contributorVersionTable">
                        <thead>
                            <tr style={{ textAlign: "center" }}>
                                <th>Name</th>
                                <th>Email</th>
                                <th onClick={() => sortFunction("files")}>
                                    Number of files authored{" "}
                                    {sort_Files === "files" &&
                                        (sort_Order === "desc" ? (
                                            <FaArrowUp />
                                        ) : (
                                            <FaArrowDown />
                                        ))}
                                </th>
                                <th>Active</th>
                                <th>Authored files</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentItems?.map((contributorVersion, id) => {
                                const innerStartIndex = (contributorVersion.innerPage - 1) * itemsPerPage;
                                const innerEndIndex = innerStartIndex + itemsPerPage;
                                const innerFileSlice = contributorVersion.filesAuthorPath.slice(innerStartIndex, innerEndIndex);
                                const innerTotalPages = Math.ceil(contributorVersion.filesAuthorPath.length / itemsPerPage);
                                return (
                                    <Fragment key={`${id}${contributorVersion.contributor.name}`}>
                                        <tr key={id} style={{ textAlign: "center" }}>
                                            <td>{contributorVersion.contributor.name}</td>
                                            <td>{contributorVersion.contributor.email}</td>
                                            <td>{contributorVersion.numberFilesAuthor}</td>
                                            <td>{contributorVersion.contributor.active ? "Yes" : "No"}</td>
                                            <td><Button onClick={() => handleExpandClick(id)} type="button">
                                                <FileCopyIcon />
                                            </Button></td>
                                        </tr>
                                        {contributorVersion.expanded ? (
                                            <tr>
                                                <td colSpan={5}>
                                                    <Table striped bordered hover>
                                                        <thead>
                                                            <tr style={{ textAlign: "center" }}>
                                                                <th>File Path</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            {innerFileSlice.map((file, innerId) => (
                                                                <tr key={innerId} style={{ textAlign: "center" }}>
                                                                    <td>{file}</td>
                                                                </tr>
                                                            ))}
                                                        </tbody>
                                                    </Table>
                                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                                        <Button onClick={() => handleInnerPreviousPage(id)} disabled={contributorVersion.innerPage === 1}>
                                                            Previous
                                                        </Button>
                                                        <span>Page {contributorVersion.innerPage} of {innerTotalPages}</span>
                                                        <Button onClick={() => handleInnerNextPage(id)} disabled={contributorVersion.innerPage === innerTotalPages}>
                                                            Next
                                                        </Button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ) : null}
                                    </Fragment>
                                );
                            })}
                        </tbody>
                    </Table>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Button onClick={handlePreviousPage} disabled={currentPage === 1}>
                            Previous
                        </Button>
                        <span>Page {currentPage} of {totalPages}</span>
                        <Button onClick={handleNextPage} disabled={currentPage === totalPages}>
                            Next
                        </Button>
                    </div>
                </Card.Body>
            </Card>
        </>
    );
};

export default ContributorVersionPaginatedTable;