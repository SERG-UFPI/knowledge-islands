import { Container, Navbar } from "react-bootstrap";

const Layout = ({ children }) => {
    return (
        <>
            <Navbar bg="primary" variant="dark">
                <Navbar.Brand>Knowledge Islands</Navbar.Brand>
            </Navbar>
            <Container>{children}</Container>
        </>
    );
};

export default Layout;