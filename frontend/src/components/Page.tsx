import type { ReactNode } from 'react'

type PageProps = {
  title: string
  children?: ReactNode
}

/** Layout base das telas da aplicação. */
export function Page({ title, children }: PageProps) {
  return (
    <section className="page">
      <header>
        <h2>{title}</h2>
      </header>
      {children}
    </section>
  )
}
