import { useState } from "react";
import { Table, Button, Card } from "react-bootstrap";

const ContributorVersionPaginatedTable = ({ contributorsVersions }) => {
    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 10;
    const totalItems = contributorsVersions.length || 0;
    const totalPages = Math.ceil(totalItems / itemsPerPage);

    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const currentItems = contributorsVersions.slice(startIndex, endIndex);
    const handlePreviousPage = () => {
        setCurrentPage((prevPage) => Math.max(prevPage - 1, 1));
    };

    const handleNextPage = () => {
        setCurrentPage((prevPage) => Math.min(prevPage + 1, totalPages));
    };

    return (
        <>
            <Card>
                <Card.Body>
                    <Card.Title>Truck Factor Developers</Card.Title>
                    <Table striped bordered hover>
                        <thead>
                            <tr style={{ textAlign: "center" }}>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Number of files authored</th>
                                <th>Percent of files(%)</th>
                                <th>Active</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentItems?.map((contributorVersion, id) => (
                                <tr key={id} style={{ textAlign: "center" }}>
                                    <td>{contributorVersion.contributor.name}</td>
                                    <td>{contributorVersion.contributor.email}</td>
                                    <td>{contributorVersion.numberFilesAuthor}</td>
                                    <td>{Math.round(contributorVersion.percentOfFilesAuthored*100)}</td>
                                    <td>{contributorVersion.contributor.active ? "Yes" : "No"}</td>
                                </tr>
                            ))}
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