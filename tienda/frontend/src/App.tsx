import { Route, Routes } from 'react-router-dom'
import { Layout } from '@/components/Layout/Layout'
import { ProtectedRoute } from '@/components/ProtectedRoute/ProtectedRoute'
import { LoginPage } from '@/pages/auth/LoginPage'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { ProductoDetallePage } from '@/pages/productos/ProductoDetallePage'
import { ProductoFormPage } from '@/pages/productos/ProductoFormPage'
import { ShopPage } from '@/pages/productos/ShopPage'

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<ShopPage />} />
        <Route path="/productos/nuevo" element={<ProductoFormPage />} />
        <Route path="/productos/:id" element={<ProductoDetallePage />} />
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
<<<<<<< HEAD
}
=======
}
>>>>>>> 20b688efdfededbfb9bce2fd8c393b1d893622c4
