import { Route, Routes } from 'react-router-dom'
import { Layout } from '@/components/Layout/Layout'
import { ProtectedRoute } from '@/components/ProtectedRoute/ProtectedRoute'
import { LoginPage } from '@/pages/auth/LoginPage'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { ProductoDetallePage } from '@/pages/productos/ProductoDetallePage'
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
        <Route path="/productos/:id" element={<ProductoDetallePage />} />
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}
