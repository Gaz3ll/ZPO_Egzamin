package pl.zpo.app.domain.algorithm;

/**
 * The domain business rule engine. Kept behind an interface so you can provide an alternative
 * implementation per subject without touching services or controllers. The default implementation
 * is {@link DefaultDomainAlgorithm}.
 *
 * <p>Deliberately pure: it takes data in ({@link DomainAlgorithmInput}) and returns data out
 * ({@link DomainAlgorithmResult}) with no I/O, so it is fully unit-testable.</p>
 */
public interface DomainAlgorithm {

    DomainAlgorithmResult evaluate(DomainAlgorithmInput input);
}
